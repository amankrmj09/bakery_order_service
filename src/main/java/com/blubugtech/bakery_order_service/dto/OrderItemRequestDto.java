package com.blubugtech.bakery_order_service.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class OrderItemRequestDto {

    // Getters and Setters
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100 per item")
    private Integer quantity;

    @Size(max = 500, message = "Special instructions must not exceed 500 characters")
    private String specialInstructions;

    // Optional: Override price (for admin orders or special pricing)
    @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid unit price format")
    private BigDecimal unitPriceOverride;

    // Constructors
    public OrderItemRequestDto() {}

    public OrderItemRequestDto(UUID productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

}
