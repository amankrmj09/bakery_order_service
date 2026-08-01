package com.blubugtech.bakery_order_service.service;

import com.blubugtech.bakery_order_service.dto.order.OrderRequest;
import com.blubugtech.bakery_order_service.dto.order.OrderResponse;
import com.blubugtech.bakery_order_service.dto.order.OrderStatusUpdateRequest;

import java.util.UUID;

public interface OrderCommandService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request);
    OrderResponse updatePaymentStatus(UUID orderId, String paymentStatus, String notes);
    OrderResponse cancelOrder(UUID orderId, String reason, boolean isAdmin);
    default OrderResponse cancelOrder(UUID orderId, String reason) {
        return cancelOrder(orderId, reason, false);
    }
}
