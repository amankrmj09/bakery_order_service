package com.blubugtech.bakery_order_service.dto;

import com.blubugtech.bakery_order_service.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class OrderItemResponseDto {

    // Getters and Setters
    private UUID id;
    private UUID productId;
    private String productSku;
    private String productName;
    private String productCategory;
    private String productDescription;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPerItem;
    private BigDecimal effectiveUnitPrice;
    private BigDecimal subtotal;
    private String specialInstructions;
    private Integer preparationTimeMinutes;
    private Integer totalPreparationTime;
    private Boolean hasDiscount;
    private LocalDateTime createdAt;

    // Constructors
    public OrderItemResponseDto() {}

    // Static factory method
    public static OrderItemResponseDto from(OrderItem orderItem) {
        OrderItemResponseDto response = new OrderItemResponseDto();
        response.id = orderItem.getId();
        response.productId = orderItem.getProductId();
        response.productSku = orderItem.getProductSku();
        response.productName = orderItem.getProductName();
        response.productCategory = orderItem.getProductCategory();
        response.productDescription = orderItem.getProductDescription();
        response.productImageUrl = orderItem.getProductImageUrl();
        response.quantity = orderItem.getQuantity();
        response.unitPrice = orderItem.getUnitPrice();
        response.discountPerItem = orderItem.getDiscountPerItem();
        response.effectiveUnitPrice = orderItem.getEffectiveUnitPrice();
        response.subtotal = orderItem.getSubtotal();
        response.specialInstructions = orderItem.getSpecialInstructions();
        response.preparationTimeMinutes = orderItem.getPreparationTimeMinutes();
        response.totalPreparationTime = orderItem.getTotalPreparationTime();
        response.hasDiscount = orderItem.hasDiscount();
        response.createdAt = orderItem.getCreatedAt();
        return response;
    }

}
