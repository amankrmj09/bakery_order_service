package com.blubugtech.bakery_order_service.kafka;

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
        logger.info("Received PaymentEvent for Order ID: {} with status: {}", event.getOrderId(), event.getStatus());
        
        try {
            String paymentStatus = event.getStatus();
            com.blubugtech.bakery_order_service.dto.OrderStatusUpdateRequestDto statusUpdate = new com.blubugtech.bakery_order_service.dto.OrderStatusUpdateRequestDto();

            switch (paymentStatus) {
                case "COMPLETED" -> {
                    statusUpdate.setStatus(com.blubugtech.bakery_order_service.entity.Order.OrderStatus.CONFIRMED);
                    statusUpdate.setNotes("Payment completed successfully");
                }
                case "FAILED" -> {
                    statusUpdate.setStatus(com.blubugtech.bakery_order_service.entity.Order.OrderStatus.CANCELLED);
                    statusUpdate.setReason("Payment failed");
                }
                case "CANCELLED" -> {
                    statusUpdate.setStatus(com.blubugtech.bakery_order_service.entity.Order.OrderStatus.CANCELLED);
                    statusUpdate.setReason("Payment cancelled");
                }
            }

            if (statusUpdate.getStatus() != null) {
                orderService.updateOrderStatus(event.getOrderId(), statusUpdate);
                logger.info("Updated order {} status to {} due to payment event", event.getOrderId(), statusUpdate.getStatus());
            } else {
                logger.info("Payment status {} for order {} - no order status change needed", paymentStatus, event.getOrderId());
            }
        } catch (Exception e) {
            logger.error("Failed to process payment event: {}", e.getMessage());
        }
    }
}
