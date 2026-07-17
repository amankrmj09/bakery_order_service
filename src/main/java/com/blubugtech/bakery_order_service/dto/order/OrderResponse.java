package com.blubugtech.bakery_order_service.dto.order;

import com.blubugtech.bakery_order_service.dto.item.OrderItemResponse;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import com.blubugtech.bakery_order_service.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class OrderResponse {

    // Getters and Setters
    private UUID id;
    private String orderNumber;
    private UUID userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private OrderStatus status;
    private DeliveryType deliveryType;
    private String deliveryAddress;
    private LocalDateTime deliveryDate;
    private String specialInstructions;
    private List<OrderItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private String discountCode;
    private BigDecimal discountPercentage;
    private Integer estimatedPreparationMinutes;
    private LocalDateTime estimatedReadyTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private Integer totalItems;
    private Boolean canBeCancelled;
    private Boolean canBeModified;

    // Constructors
    public OrderResponse() {}

}
