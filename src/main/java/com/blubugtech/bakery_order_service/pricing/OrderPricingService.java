package com.blubugtech.bakery_order_service.pricing;

import com.blubugtech.bakery_order_service.entity.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderPricingService {

    @Value("${order.tax.rate:0.08}")
    private BigDecimal taxRate;

    @Value("${order.delivery.default-time-minutes:60}")
    private Integer defaultDeliveryTimeMinutes;

    public void applyPricingAndTiming(Order order, String discountCode) {
        calculatePreparationTime(order);
        applyDiscounts(order, discountCode);
        order.calculateTotals(taxRate);
        setDeliveryFee(order);
        order.calculateTotals(taxRate);
    }

    private void calculatePreparationTime(Order order) {
        int totalPreparationMinutes = order.getOrderItems().stream()
                .mapToInt(item -> item.getTotalPreparationTime() != null ? item.getTotalPreparationTime() : 0)
                .max()
                .orElse(defaultDeliveryTimeMinutes);

        order.setEstimatedPreparationMinutes(totalPreparationMinutes);
        order.setEstimatedReadyTime(LocalDateTime.now().plusMinutes(totalPreparationMinutes));
    }

    private void applyDiscounts(Order order, String discountCode) {
        if (discountCode != null && !discountCode.trim().isEmpty()) {
            // Apply discount based on code
            // This would typically involve checking a discount table
        }
    }

    private void setDeliveryFee(Order order) {
        if (order.getDeliveryType() == com.blubugtech.bakery_order_service.enums.DeliveryType.DELIVERY) {
            order.setDeliveryFee(new BigDecimal("5.00")); // Fixed delivery fee for now
        } else {
            order.setDeliveryFee(BigDecimal.ZERO);
        }
    }
}
