package com.blubugtech.bakery_order_service.pricing.strategy;

import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DeliveryOrderCalculationStrategy implements OrderCalculationStrategy {

    @Override
    public boolean supports(DeliveryType deliveryType) {
        return deliveryType != DeliveryType.PICKUP;
    }

    @Override
    public void calculateTotals(Order order) {
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);

        // For delivery, tax might be 8% tax.
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.08)).setScale(2, java.math.RoundingMode.HALF_UP);
        order.setTaxAmount(tax);
        
        // Delivery fee is assumed to be calculated elsewhere and set on the order (like OrderPricingService)
        BigDecimal deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO;

        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(tax).add(deliveryFee).subtract(discount);
        
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        
        order.setTotalAmount(total);
    }
}
