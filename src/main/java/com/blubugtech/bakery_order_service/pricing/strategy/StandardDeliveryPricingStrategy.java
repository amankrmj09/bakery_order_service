package com.blubugtech.bakery_order_service.pricing.strategy;

import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StandardDeliveryPricingStrategy implements DeliveryPricingStrategy {

    @Override
    public boolean supports(DeliveryType deliveryType) {
        return DeliveryType.DELIVERY.equals(deliveryType);
    }

    @Override
    public BigDecimal calculateFee(Order order) {
        return new BigDecimal("50.00"); // Fixed delivery fee for now
    }
}
