package com.blubugtech.bakery_order_service.integration.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.common.messaging.event.OrderEvent;
import org.blubakery.common.messaging.contract.messaging.OrderPayload;
import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.enums.OrderStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class KafkaOrderEventPublisher implements com.blubugtech.bakery_order_service.event.OrderEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @org.springframework.beans.factory.annotation.Value("${kafka.topic.order-events}")
    private String orderEventsTopic;

    @org.springframework.beans.factory.annotation.Value("${kafka.topic.payment-requests}")
    private String paymentRequestsTopic;
    
    public KafkaOrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @Override
    public void publishOrderCreated(Order order, Map<String, Object> metadata) {
        OrderEvent event = buildOrderEvent(order, "ORDER_CREATED", metadata);
        log.info("Publishing OrderCreated event for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.common.messaging.constants.KafkaTopics.ORDERS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }
    
    @Override
    public void publishOrderStatusUpdated(Order order, OrderStatus oldStatus, OrderStatus newStatus, Map<String, Object> metadata) {
        OrderEvent event = buildOrderEvent(order, "ORDER_STATUS_UPDATED", metadata);
        log.info("Publishing OrderStatusUpdated event for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.common.messaging.constants.KafkaTopics.ORDERS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }

    @Override
    public void publishPaymentStatusUpdated(Order order, Map<String, Object> metadata) {
        OrderEvent event = buildOrderEvent(order, "ORDER_PAYMENT_STATUS_UPDATED", metadata);
        log.info("Publishing PaymentStatusUpdated event for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.common.messaging.constants.KafkaTopics.ORDERS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }
    
    @Override
    public void publishInvoiceGenerated(Order order, String invoiceUrl) {
        OrderEvent event = buildOrderEvent(order, "INVOICE_GENERATED", null);
        event.getPayload().setInvoiceUrl(invoiceUrl);
        log.info("Publishing InvoiceGenerated event for order ID: {}", event.getPayload().getOrderId());
        kafkaTemplate.send(org.blubakery.common.messaging.constants.KafkaTopics.ORDERS_TOPIC, event.getPayload().getOrderId().toString(), event);
    }
    
    private OrderEvent buildOrderEvent(Order order, String eventType, Map<String, Object> metadata) {
        Boolean cancelledByAdmin = metadata != null && metadata.containsKey("cancelledByAdmin") ? (Boolean) metadata.get("cancelledByAdmin") : null;
        OrderPayload payload = OrderPayload.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .customerEmail(order.getCustomerEmail())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .deliveryAddress(order.getDeliveryAddress())
                .cancellationReason(order.getCancellationReason())
                .timestamp(LocalDateTime.now())
                .cancelledByAdmin(cancelledByAdmin)
                .build();
        return OrderEvent.builder()
                .eventType(eventType)
                .payload(payload)
                .metadata(metadata)
                .build();
    }
}
