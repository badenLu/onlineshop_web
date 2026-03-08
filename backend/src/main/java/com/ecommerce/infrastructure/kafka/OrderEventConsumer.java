package com.ecommerce.infrastructure.kafka;

import com.ecommerce.order.dto.OrderEvent;
import com.ecommerce.product.repository.ProductSkuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ProductSkuRepository productSkuRepository;

    /**
     * Async DB stock deduction after order created.
     * Redis already deducted instantly; this syncs to MySQL.
     */
    @KafkaListener(topics = "order.created", groupId = "inventory-group")
    @Transactional
    public void handleOrderCreated(OrderEvent event) {
        log.info("Consuming order.created: orderNo={}", event.getOrderNo());
        for (OrderEvent.OrderItemEvent item : event.getItems()) {
            int affected = productSkuRepository.deductStock(item.getSkuId(), item.getQuantity());
            if (affected == 0) {
                log.warn("DB stock deduction failed: skuId={}, qty={}", item.getSkuId(), item.getQuantity());
            }
        }
        log.info("DB stock synced for order: {}", event.getOrderNo());
    }

    /**
     * Restore DB stock when order is cancelled.
     */
    @KafkaListener(topics = "order.cancelled", groupId = "inventory-group")
    @Transactional
    public void handleOrderCancelled(OrderEvent event) {
        log.info("Consuming order.cancelled: orderNo={}", event.getOrderNo());
        for (OrderEvent.OrderItemEvent item : event.getItems()) {
            productSkuRepository.restoreStock(item.getSkuId(), item.getQuantity());
        }
        log.info("DB stock restored for cancelled order: {}", event.getOrderNo());
    }

    /**
     * Handle post-payment logic (e.g., update sales count, send notification).
     */
    @KafkaListener(topics = "order.paid", groupId = "notification-group")
    public void handleOrderPaid(OrderEvent event) {
        log.info("Consuming order.paid: orderNo={}, amount={}", event.getOrderNo(), event.getTotalAmount());
        // Here you could: send email, update sales ranking, trigger shipping, etc.
    }
}