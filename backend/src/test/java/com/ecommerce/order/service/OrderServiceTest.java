package com.ecommerce.order.service;

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
import com.ecommerce.order.service.impl.OrderServiceImpl;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductSku;
import com.ecommerce.product.repository.ProductSkuRepository;
import com.ecommerce.product.service.InventoryService;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.entity.UserAddress;
import com.ecommerce.user.repository.UserAddressRepository;
import com.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductSkuRepository productSkuRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserAddressRepository userAddressRepository;
    @Mock private InventoryService inventoryService;
    @Mock private CartService cartService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private UserAddress testAddress;
    private ProductSku testSku;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").email("test@test.com").build();

        testAddress = UserAddress.builder()
                .id(1L).receiverName("John").phone("+49123456")
                .province("Baden-Württemberg").city("Stuttgart")
                .district("Mitte").detailAddress("Hauptstraße 1")
                .build();

        testProduct = Product.builder()
                .id(1L).name("Test Product").mainImage("image.jpg")
                .build();

        testSku = ProductSku.builder()
                .id(1L).product(testProduct).skuCode("SKU001")
                .attributes(Map.of("color", "black", "size", "M"))
                .price(new BigDecimal("29.99")).stock(100)
                .build();
    }

    @Nested
    @DisplayName("Create Order")
    class CreateOrder {

        @Test
        @DisplayName("Should create order successfully")
        void createOrder_Success() {
            OrderRequest request = buildOrderRequest();

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                    .thenReturn(true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userAddressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
            when(productSkuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(inventoryService.deductStock(1L, 2)).thenReturn(true);
            // when(productSkuRepository.deductStock(1L, 2)).thenReturn(1);
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(1L);
                return o;
            });

            OrderResponse response = orderService.createOrder(1L, request);

            assertThat(response).isNotNull();
            assertThat(response.getOrderNo()).isNotEmpty();
            assertThat(response.getStatus()).isEqualTo("PENDING_PAYMENT");
            assertThat(response.getReceiverName()).isEqualTo("John");
            verify(inventoryService).deductStock(1L, 2);
            verify(cartService).removeFromCart(1L, 1L);
        }

        @Test
        @DisplayName("Should fail when stock is insufficient")
        void createOrder_InsufficientStock() {
            OrderRequest request = buildOrderRequest();

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                    .thenReturn(true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userAddressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
            when(productSkuRepository.findById(1L)).thenReturn(Optional.of(testSku));
            when(inventoryService.deductStock(1L, 2)).thenReturn(false);

            assertThatThrownBy(() -> orderService.createOrder(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("Should prevent duplicate submission")
        void createOrder_DuplicateSubmission() {
            OrderRequest request = buildOrderRequest();

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                    .thenReturn(false); // Already exists = duplicate

            assertThatThrownBy(() -> orderService.createOrder(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Duplicate");
        }
    }

    @Nested
    @DisplayName("Cancel Order")
    class CancelOrder {

        @Test
        @DisplayName("Should cancel pending order and restore stock")
        void cancelOrder_Success() {
            OrderItem item = OrderItem.builder().skuId(1L).quantity(2).build();
            Order order = Order.builder()
                    .id(1L).orderNo("20240101000001").status(OrderStatus.PENDING_PAYMENT)
                    .items(List.of(item))
                    .build();
            order.setUser(testUser);

            when(orderRepository.findByOrderNoAndUserId("20240101000001", 1L))
                    .thenReturn(Optional.of(order));

            orderService.cancelOrder(1L, "20240101000001");

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
            verify(inventoryService).restoreStock(1L, 2);
            verify(orderEventProducer).sendOrderCancelled(any(OrderEvent.class));
            // verify(productSkuRepository).restoreStock(1L, 2);
        }

        @Test
        @DisplayName("Should reject cancellation of paid order")
        void cancelOrder_AlreadyPaid() {
            Order order = Order.builder()
                    .id(1L).orderNo("20240101000001").status(OrderStatus.PAID)
                    .build();
            order.setUser(testUser);

            when(orderRepository.findByOrderNoAndUserId("20240101000001", 1L))
                    .thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(1L, "20240101000001"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("pending payment");
        }
    }

    @Nested
    @DisplayName("Pay Order")
    class PayOrder {
        @Test
        @DisplayName("Should process payment successfully")
        void payOrder_Success() {
            Order order = Order.builder()
                    .id(1L).orderNo("20240101000001").status(OrderStatus.PENDING_PAYMENT)
                    .items(new ArrayList<>())
                    .build();
            order.setUser(testUser);

            when(orderRepository.findByOrderNoAndUserId("20240101000001", 1L))
                    .thenReturn(Optional.of(order));

            PaymentRequest payReq = new PaymentRequest();
            payReq.setPaymentType("CREDIT_CARD");

            orderService.payOrder(1L, "20240101000001", payReq);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getPaymentType()).isEqualTo("CREDIT_CARD");
            assertThat(order.getPaymentTime()).isNotNull();
            verify(orderEventProducer).sendOrderPaid(any(OrderEvent.class));
        }
    }

    // Helper
    private OrderRequest buildOrderRequest() {
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setSkuId(1L);
        itemReq.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setAddressId(1L);
        request.setItems(List.of(itemReq));
        request.setRemark("Test order");
        return request;
    }
}