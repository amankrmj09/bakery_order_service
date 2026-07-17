package com.blubugtech.bakery_order_service.dto.item;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class OrderItemResponse {

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
    public OrderItemResponse() {}

}
