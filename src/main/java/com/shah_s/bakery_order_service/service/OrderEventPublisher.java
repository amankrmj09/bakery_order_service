package com.shah_s.bakery_order_service.service;

import org.devofblue.common.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void publishOrderCreated(OrderEvent event) {
        logger.info("Publishing OrderCreated event for order ID: {}", event.getOrderId());
        kafkaTemplate.send("order-events", event.getOrderId().toString(), event);
    }
    
    public void publishOrderStatusUpdated(OrderEvent event) {
        logger.info("Publishing OrderStatusUpdated event for order ID: {}", event.getOrderId());
        kafkaTemplate.send("order-events", event.getOrderId().toString(), event);
    }

    public void publishPaymentRequested(org.devofblue.common.event.PaymentRequestedEvent event) {
        logger.info("Publishing PaymentRequestedEvent for order ID: {}", event.getOrderId());
        kafkaTemplate.send("payment-requests", event.getOrderId().toString(), event);
    }
}
