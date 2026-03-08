package com.ecommerce.order.service.impl;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.infrastructure.kafka.OrderEventProducer;
import com.ecommerce.order.dto.OrderEvent;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PaymentRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.CartService;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductSku;
import com.ecommerce.product.repository.ProductSkuRepository;
import com.ecommerce.product.service.InventoryService;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.entity.UserAddress;
import com.ecommerce.user.repository.UserAddressRepository;
import com.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductSkuRepository productSkuRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final InventoryService inventoryService;
    private final CartService cartService;
    private final StringRedisTemplate redisTemplate;

    private static final String IDEMPOTENCY_PREFIX = "order:idempotent:";
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    private final OrderEventProducer orderEventProducer;

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, OrderRequest request) {
        // 1. Idempotency check: prevent duplicate submissions
        String idempotencyKey = IDEMPOTENCY_PREFIX + userId + ":" + request.hashCode();
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", 10, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isNew)) {
            throw new BusinessException("Duplicate order submission, please wait");
        }

        try {
            // 2. Validate user and address
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.notFound("User"));
            UserAddress address = userAddressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> BusinessException.notFound("Address"));

            // 3. Build order items and deduct inventory
            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<Long> deductedSkuIds = new ArrayList<>(); // track for rollback

            for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
                ProductSku sku = productSkuRepository.findById(itemReq.getSkuId())
                        .orElseThrow(() -> BusinessException.notFound("SKU"));
                Product product = sku.getProduct();

                // Deduct stock via Redis Lua (atomic, no oversell)
                boolean success = inventoryService.deductStock(sku.getId(), itemReq.getQuantity());
                if (!success) {
                    // Rollback previously deducted items
                    rollbackInventory(deductedSkuIds, orderItems);
                    throw BusinessException.insufficientStock();
                }
                deductedSkuIds.add(sku.getId());

                BigDecimal itemTotal = sku.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));

                OrderItem orderItem = OrderItem.builder()
                        .productId(product.getId())
                        .skuId(sku.getId())
                        .productName(product.getName())
                        .productImage(product.getMainImage())
                        .skuAttributes(sku.getAttributes().toString())
                        .unitPrice(sku.getPrice())
                        .quantity(itemReq.getQuantity())
                        .totalPrice(itemTotal)
                        .build();

                orderItems.add(orderItem);
                totalAmount = totalAmount.add(itemTotal);
            }

            // 4. Create order
            BigDecimal freight = calculateFreight(totalAmount);
            BigDecimal payAmount = totalAmount.add(freight);

            Order order = Order.builder()
                    .user(user)
                    .orderNo(generateOrderNo())
                    .totalAmount(totalAmount)
                    .payAmount(payAmount)
                    .freight(freight)
                    .status(OrderStatus.PENDING_PAYMENT)
                    .receiverName(address.getReceiverName())
                    .receiverPhone(address.getPhone())
                    .receiverAddress(buildFullAddress(address))
                    .remark(request.getRemark())
                    .items(orderItems)
                    .build();

            // Set order reference on each item
            orderItems.forEach(item -> item.setOrder(order));

            orderRepository.save(order);

            // 5. Deduct stock in DB (final consistency)
//            for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
//                int affected = productSkuRepository.deductStock(itemReq.getSkuId(), itemReq.getQuantity());
//                if (affected == 0) {
//                    log.warn("DB stock deduction failed for SKU: {}, Redis already deducted", itemReq.getSkuId());
//                }
//            }
            OrderEvent event = buildOrderEvent("ORDER_CREATED", order, request.getItems());
            orderEventProducer.sendOrderCreated(event);

            // 6. Clear purchased items from cart
            for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
                cartService.removeFromCart(userId, itemReq.getSkuId());
            }

            log.info("Order created: orderNo={}, userId={}, amount={}", order.getOrderNo(), userId, payAmount);
            return toResponse(order);

        } catch (BusinessException e) {
            redisTemplate.delete(idempotencyKey);
            throw e;
        }
    }

    @Override
    public OrderResponse getOrder(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNoAndUserId(orderNo, userId)
                .orElseThrow(() -> BusinessException.notFound("Order"));
        return toResponse(order);
    }

    @Override
    public Page<OrderResponse> listOrders(Long userId, String status, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByUserIdAndStatus(userId, OrderStatus.valueOf(status), pageable);
        } else {
            orders = orderRepository.findByUserId(userId, pageable);
        }

        return orders.map(this::toListResponse);
    }

    private OrderResponse toListResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .totalAmount(order.getTotalAmount())
                .payAmount(order.getPayAmount())
                .freight(order.getFreight())
                .status(order.getStatus().name())
                .paymentType(order.getPaymentType())
                .paymentTime(order.getPaymentTime())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .remark(order.getRemark())
                .items(null)
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNoAndUserId(orderNo, userId)
                .orElseThrow(() -> BusinessException.notFound("Order"));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException("Only pending payment orders can be cancelled");
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        // Restore inventory
//        for (OrderItem item : order.getItems()) {
//            inventoryService.restoreStock(item.getSkuId(), item.getQuantity());
//            productSkuRepository.restoreStock(item.getSkuId(), item.getQuantity());
//        }

        for (OrderItem item : order.getItems()) {
            inventoryService.restoreStock(item.getSkuId(), item.getQuantity());
        }

        // 恢复 DB 库存（异步，通过 Kafka）
        OrderEvent event = OrderEvent.builder()
                .eventType("ORDER_CANCELLED")
                .orderNo(order.getOrderNo())
                .userId(userId)
                .timestamp(java.time.LocalDateTime.now())
                .items(order.getItems().stream()
                        .map(item -> OrderEvent.OrderItemEvent.builder()
                                .skuId(item.getSkuId())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(java.util.stream.Collectors.toList()))
                .build();
        orderEventProducer.sendOrderCancelled(event);

        log.info("Order cancelled: orderNo={}", orderNo);
    }

    @Override
    @Transactional
    public void payOrder(Long userId, String orderNo, PaymentRequest request) {
        Order order = orderRepository.findByOrderNoAndUserId(orderNo, userId)
                .orElseThrow(() -> BusinessException.notFound("Order"));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException("Order is not in pending payment status");
        }

        // Simulate payment success
        order.setStatus(OrderStatus.PAID);
        order.setPaymentType(request.getPaymentType());
        order.setPaymentTime(LocalDateTime.now());
        orderRepository.save(order);

        // 发送支付成功事件
        OrderEvent event = OrderEvent.builder()
                .eventType("ORDER_PAID")
                .orderNo(order.getOrderNo())
                .userId(userId)
                .totalAmount(order.getPayAmount())
                .timestamp(LocalDateTime.now())
                .build();
        orderEventProducer.sendOrderPaid(event);

        log.info("Order paid: orderNo={}, paymentType={}", orderNo, request.getPaymentType());
    }

    @Override
    @Transactional
    public void confirmReceipt(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNoAndUserId(orderNo, userId)
                .orElseThrow(() -> BusinessException.notFound("Order"));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("Order has not been delivered yet");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setReceiveTime(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order completed: orderNo={}", orderNo);
    }

    // --- Helper methods ---

    private void rollbackInventory(List<Long> deductedSkuIds, List<OrderItem> items) {
        for (int i = 0; i < deductedSkuIds.size(); i++) {
            inventoryService.restoreStock(deductedSkuIds.get(i), items.get(i).getQuantity());
        }
    }

    private BigDecimal calculateFreight(BigDecimal totalAmount) {
        // Free shipping over 50 EUR
        return totalAmount.compareTo(new BigDecimal("50")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("4.99");
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int seq = SEQUENCE.incrementAndGet() % 1000000;
        return timestamp + String.format("%06d", seq);
    }

    private String buildFullAddress(UserAddress address) {
        return String.join(" ",
                address.getProvince(),
                address.getCity(),
                address.getDistrict(),
                address.getDetailAddress());
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .skuId(item.getSkuId())
                        .productName(item.getProductName())
                        .productImage(item.getProductImage())
                        .skuAttributes(item.getSkuAttributes())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .totalAmount(order.getTotalAmount())
                .payAmount(order.getPayAmount())
                .freight(order.getFreight())
                .status(order.getStatus().name())
                .paymentType(order.getPaymentType())
                .paymentTime(order.getPaymentTime())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .remark(order.getRemark())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderEvent buildOrderEvent(String type, Order order, List<OrderRequest.OrderItemRequest> items) {
        return OrderEvent.builder()
                .eventType(type)
                .orderNo(order.getOrderNo())
                .userId(order.getUser().getId())
                .totalAmount(order.getPayAmount())
                .timestamp(LocalDateTime.now())
                .items(items.stream()
                        .map(i -> OrderEvent.OrderItemEvent.builder()
                                .skuId(i.getSkuId())
                                .quantity(i.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}