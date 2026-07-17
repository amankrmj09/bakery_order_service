package com.blubugtech.bakery_order_service.mapper;

import com.blubugtech.bakery_order_service.dto.order.OrderResponse;
import com.blubugtech.bakery_order_service.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "items", source = "orderItems")
    @Mapping(target = "canBeCancelled", ignore = true)
    @Mapping(target = "canBeModified", ignore = true)
    OrderResponse toResponse(Order order);
}
