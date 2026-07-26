package com.blubugtech.bakery_order_service.integration.kafka.consumer;

import com.blubugtech.common.event.PaymentEvent;
import com.blubugtech.bakery_order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class PaymentEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventConsumer.class);
    private final OrderService orderService;

    public PaymentEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "${kafka.topic.payment-events}", groupId = "order-service-group")
    public void consume(PaymentEvent event) {
        logger.info("Received PaymentEvent for Order ID: {} with status: {}", event.getPayload().getOrderId(), event.getPayload().getStatus());
        
        try {
            String paymentStatus = event.getPayload().getStatus();
            orderService.updatePaymentStatus(event.getPayload().getOrderId(), paymentStatus, "Payment event: " + paymentStatus);
            logger.info("Updated order {} payment status to {} due to payment event", event.getPayload().getOrderId(), paymentStatus);
        } catch (Exception e) {
            logger.error("Failed to process payment event: {}", e.getMessage());
        }
    }
}
