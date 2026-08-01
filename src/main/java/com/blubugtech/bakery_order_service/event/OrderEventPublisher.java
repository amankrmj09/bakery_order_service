package com.blubugtech.bakery_order_service.event;

import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.enums.OrderStatus;
import java.util.Map;

public interface OrderEventPublisher {
    void publishOrderCreated(Order order, Map<String, Object> metadata);
    void publishOrderStatusUpdated(Order order, OrderStatus oldStatus, OrderStatus newStatus, Map<String, Object> metadata);
    void publishPaymentStatusUpdated(Order order, Map<String, Object> metadata);
    void publishInvoiceGenerated(Order order, String invoiceUrl);
}
