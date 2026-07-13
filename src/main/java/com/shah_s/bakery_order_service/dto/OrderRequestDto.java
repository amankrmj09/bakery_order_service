package com.shah_s.bakery_order_service.dto;

import com.shah_s.bakery_order_service.entity.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class OrderRequestDto {

    // Getters and Setters
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 200, message = "Customer name must not exceed 200 characters")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String customerEmail;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String customerPhone;

    @NotNull(message = "Delivery type is required")
    private Order.DeliveryType deliveryType;

    private String deliveryAddress;

    private LocalDateTime deliveryDate;

    @Size(max = 1000, message = "Special instructions must not exceed 1000 characters")
    private String specialInstructions;

    @Valid
    @NotEmpty(message = "Order must contain at least one item")
    @Size(max = 50, message = "Order cannot contain more than 50 items")
    private List<OrderItemRequestDto> items = new ArrayList<>();

    @Size(max = 50, message = "Discount code must not exceed 50 characters")
    private String discountCode;

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // "CASH", "CARD", "DIGITAL_WALLET", etc.

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    private BigDecimal paymentAmount;

    private String currencyCode = "USD";

    // Card payment details (optional)
    private String cardLastFour;
    private String cardBrand;
    private String cardType;

    // Digital wallet details (optional)
    private String digitalWalletProvider;

    // Bank details (optional)
    private String bankName;

    private String paymentNotes;

    // Constructors
    public OrderRequestDto() {}

}
