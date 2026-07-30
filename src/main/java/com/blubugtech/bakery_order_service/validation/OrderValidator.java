package com.blubugtech.bakery_order_service.validation;

import com.blubugtech.bakery_order_service.dto.order.OrderRequest;
import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.exception.OrderServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderValidator {

    @Value("${order.limits.max-items-per-order:50}")
    private Integer maxItemsPerOrder;

    @Value("${order.limits.max-order-value:500.00}")
    private BigDecimal maxOrderValue;

    public void validateOrderRequest(OrderRequest request) {
        if (request.getItems().size() > maxItemsPerOrder) {
            throw new OrderServiceException("Order cannot contain more than " + maxItemsPerOrder + " items");
        }

        if (request.getDeliveryType() == com.blubugtech.bakery_order_service.enums.DeliveryType.DELIVERY &&
                (request.getDeliveryAddress() == null || request.getDeliveryAddress().trim().isEmpty())) {
            throw new OrderServiceException("Delivery address is required for delivery orders");
        }
    }

    public void validatePaymentAmount(OrderRequest request, Order order) {
        if (request.getPaymentAmount() == null) {
            throw new OrderServiceException("Payment amount is required.");
        }
        // Allow a tiny tolerance (0.01) for floating-point rounding between frontend/backend
        BigDecimal diff = request.getPaymentAmount().subtract(order.getTotalAmount()).abs();
        if (diff.compareTo(new BigDecimal("0.01")) > 0) {
            throw new OrderServiceException("Payment amount must match the order total: " + order.getTotalAmount());
        }
    }

    public void validateOrderLimits(Order order) {
        if (order.getTotalAmount().compareTo(maxOrderValue) > 0) {
            throw new OrderServiceException("Order value exceeds maximum limit of ₹" + maxOrderValue);
        }
    }
}
