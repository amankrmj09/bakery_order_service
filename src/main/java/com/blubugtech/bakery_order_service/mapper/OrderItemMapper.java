package com.blubugtech.bakery_order_service.mapper;

import com.blubugtech.bakery_order_service.dto.item.OrderItemResponse;
import com.blubugtech.bakery_order_service.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target = "hasDiscount", ignore = true)
    OrderItemResponse toResponse(OrderItem orderItem);
}
