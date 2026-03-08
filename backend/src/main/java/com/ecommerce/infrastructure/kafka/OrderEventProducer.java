package com.ecommerce.infrastructure.kafka;

import com.ecommerce.order.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void sendOrderCreated(OrderEvent event) {
        kafkaTemplate.send("order.created", event.getOrderNo(), event);
        log.info("Sent order.created event: orderNo={}", event.getOrderNo());
    }

    public void sendOrderCancelled(OrderEvent event) {
        kafkaTemplate.send("order.cancelled", event.getOrderNo(), event);
        log.info("Sent order.cancelled event: orderNo={}", event.getOrderNo());
    }

    public void sendOrderPaid(OrderEvent event) {
        kafkaTemplate.send("order.paid", event.getOrderNo(), event);
        log.info("Sent order.paid event: orderNo={}", event.getOrderNo());
    }
}