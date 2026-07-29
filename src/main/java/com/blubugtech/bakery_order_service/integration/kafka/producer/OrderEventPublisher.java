package com.blubugtech.bakery_order_service.integration.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.bakery_common_libs.event.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @org.springframework.beans.factory.annotation.Value("${kafka.topic.order-events}")
    

    @org.springframework.beans.factory.annotation.Value("${kafka.topic.payment-requests}")
    
    
    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void publishOrderCreated(OrderEvent event) {
        log.info("Publishing OrderCreated event for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.bakery_common_libs.constants.KafkaTopics.ORDERS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }
    
    public void publishOrderStatusUpdated(OrderEvent event) {
        log.info("Publishing OrderStatusUpdated event for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.bakery_common_libs.constants.KafkaTopics.ORDERS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }

    public void publishPaymentRequested(com.blubugtech.common.event.PaymentRequestedEvent event) {
        log.info("Publishing PaymentRequestedEvent for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.bakery_common_libs.constants.KafkaTopics.PAYMENT_REQUESTS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }
}
