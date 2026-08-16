package com.blubugtech.bakery_order_service.pricing.strategy;

import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PickupOrderCalculationStrategy implements OrderCalculationStrategy {

    @Override
    public boolean supports(DeliveryType deliveryType) {
        return deliveryType == DeliveryType.PICKUP;
    }

    @Override
    public void calculateTotals(Order order) {
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);

        // Use the tax already provided by the cart service (product-level tax rates),
        // NOT a hardcoded flat rate — this ensures paymentAmount validation passes.
        BigDecimal tax = order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO;
        order.setTaxAmount(tax);

        // No delivery fee for pickup
        order.setDeliveryFee(BigDecimal.ZERO);

        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(tax).subtract(discount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }

        order.setTotalAmount(total);
    }
}
