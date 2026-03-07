package com.ecommerce.order.service;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.product.repository.ProductSkuRepository;
import com.ecommerce.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final ProductSkuRepository productSkuRepository;

    /**
     * Auto-cancel unpaid orders after 30 minutes.
     * Runs every 60 seconds.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoCancelUnpaidOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(
                OrderStatus.PENDING_PAYMENT, threshold);

        for (Order order : expiredOrders) {
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);

            // Restore inventory
            for (OrderItem item : order.getItems()) {
                inventoryService.restoreStock(item.getSkuId(), item.getQuantity());
                productSkuRepository.restoreStock(item.getSkuId(), item.getQuantity());
            }

            log.info("Auto-cancelled unpaid order: orderNo={}", order.getOrderNo());
        }

        if (!expiredOrders.isEmpty()) {
            log.info("Auto-cancelled {} unpaid orders", expiredOrders.size());
        }
    }
}