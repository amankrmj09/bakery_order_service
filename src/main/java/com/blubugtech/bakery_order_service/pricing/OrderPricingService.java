package com.blubugtech.bakery_order_service.pricing;

import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import com.blubugtech.bakery_order_service.pricing.strategy.DeliveryPricingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderPricingService {

    @Value("${order.delivery.default-time-minutes:60}")
    private Integer defaultDeliveryTimeMinutes;

    private final List<DeliveryPricingStrategy> pricingStrategies;
    private final List<com.blubugtech.bakery_order_service.pricing.strategy.OrderCalculationStrategy> calculationStrategies;

    public OrderPricingService(List<DeliveryPricingStrategy> pricingStrategies,
                               List<com.blubugtech.bakery_order_service.pricing.strategy.OrderCalculationStrategy> calculationStrategies) {
        this.pricingStrategies = pricingStrategies;
        this.calculationStrategies = calculationStrategies;
    }

    public void applyPricingAndTiming(Order order, String discountCode) {
        calculatePreparationTime(order);
        applyDiscounts(order, discountCode);
        setDeliveryFee(order);
        
        calculationStrategies.stream()
                .filter(strategy -> strategy.supports(order.getDeliveryType()))
                .findFirst()
                .ifPresentOrElse(
                        strategy -> strategy.calculateTotals(order),
                        () -> order.calculateTotals() // Fallback just in case
                );
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
        BigDecimal fee = pricingStrategies.stream()
                .filter(strategy -> strategy.supports(order.getDeliveryType()))
                .findFirst()
                .map(strategy -> strategy.calculateFee(order))
                .orElse(BigDecimal.ZERO);
        
        order.setDeliveryFee(fee);
    }
}
