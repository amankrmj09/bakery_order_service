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
            com.blubugtech.bakery_order_service.dto.order.OrderStatusUpdateRequest statusUpdate = new com.blubugtech.bakery_order_service.dto.order.OrderStatusUpdateRequest();

            switch (paymentStatus) {
                case "COMPLETED" -> {
                    statusUpdate.setStatus(com.blubugtech.bakery_order_service.enums.OrderStatus.CONFIRMED);
                    statusUpdate.setNotes("Payment completed successfully");
                }
                case "FAILED" -> {
                    statusUpdate.setStatus(com.blubugtech.bakery_order_service.enums.OrderStatus.CANCELLED);
                    statusUpdate.setReason("Payment failed");
                }
                case "CANCELLED" -> {
                    statusUpdate.setStatus(com.blubugtech.bakery_order_service.enums.OrderStatus.CANCELLED);
                    statusUpdate.setReason("Payment cancelled");
                }
            }

            if (statusUpdate.getStatus() != null) {
                orderService.updateOrderStatus(event.getPayload().getOrderId(), statusUpdate);
                logger.info("Updated order {} status to {} due to payment event", event.getPayload().getOrderId(), statusUpdate.getStatus());
            } else {
                logger.info("Payment status {} for order {} - no order status change needed", paymentStatus, event.getPayload().getOrderId());
            }
        } catch (Exception e) {
            logger.error("Failed to process payment event: {}", e.getMessage());
        }
    }
}
