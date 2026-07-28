package com.blubugtech.bakery_order_service.integration.kafka.producer;

import org.blubakery.bakery_common_libs.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @org.springframework.beans.factory.annotation.Value("${kafka.topic.order-events}")
    

    @org.springframework.beans.factory.annotation.Value("${kafka.topic.payment-requests}")
    
    
    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void publishOrderCreated(OrderEvent event) {
        logger.info("Publishing OrderCreated event for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.bakery_common_libs.constants.KafkaTopics.ORDERS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }
    
    public void publishOrderStatusUpdated(OrderEvent event) {
        logger.info("Publishing OrderStatusUpdated event for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.bakery_common_libs.constants.KafkaTopics.ORDERS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }

    public void publishPaymentRequested(com.blubugtech.common.event.PaymentRequestedEvent event) {
        logger.info("Publishing PaymentRequestedEvent for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.bakery_common_libs.constants.KafkaTopics.PAYMENT_REQUESTS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }
}
