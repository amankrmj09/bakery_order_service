package com.blubugtech.bakery_order_service.pricing.strategy;

import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PickupPricingStrategy implements DeliveryPricingStrategy {

    @Override
    public boolean supports(DeliveryType deliveryType) {
        return !DeliveryType.DELIVERY.equals(deliveryType);
    }

    @Override
    public BigDecimal calculateFee(Order order) {
        return BigDecimal.ZERO;
    }
}
