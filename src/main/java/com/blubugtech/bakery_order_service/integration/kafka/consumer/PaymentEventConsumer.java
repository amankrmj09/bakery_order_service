package com.blubugtech.bakery_order_service.integration.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.bakery_common_libs.event.PaymentEvent;
import com.blubugtech.bakery_order_service.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
@Slf4j
public class PaymentEventConsumer {
    private final OrderService orderService;

    public PaymentEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = org.blubakery.bakery_common_libs.constants.KafkaTopics.PAYMENTS_TOPIC, groupId = "order-service-group")
    public void consume(PaymentEvent event) {
        log.info("Received PaymentEvent for Order ID: {} with status: {}", event.getPayload().getOrderId(), event.getPayload().getStatus());
        
        try {
            String paymentStatus = event.getPayload().getStatus();
            orderService.updatePaymentStatus(event.getPayload().getOrderId(), paymentStatus, "Payment event: " + paymentStatus);
            log.info("Updated order {} payment status to {} due to payment event", event.getPayload().getOrderId(), paymentStatus);
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", e.getMessage());
        }
    }
}
