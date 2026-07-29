package com.blubugtech.bakery_order_service.integration.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.common.messaging.event.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @org.springframework.beans.factory.annotation.Value("${kafka.topic.order-events}")
    private String orderEventsTopic;

    @org.springframework.beans.factory.annotation.Value("${kafka.topic.payment-requests}")
    private String paymentRequestsTopic;
    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void publishOrderCreated(OrderEvent event) {
        log.info("Publishing OrderCreated event for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.common.messaging.constants.KafkaTopics.ORDERS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }
    
    public void publishOrderStatusUpdated(OrderEvent event) {
        log.info("Publishing OrderStatusUpdated event for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.common.messaging.constants.KafkaTopics.ORDERS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }

    public void publishPaymentRequested(org.blubakery.common.messaging.event.PaymentRequestedEvent event) {
        log.info("Publishing PaymentRequestedEvent for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.common.messaging.constants.KafkaTopics.PAYMENT_REQUESTS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }
}
