package com.shah_s.bakery_order_service.dto;

import com.shah_s.bakery_order_service.entity.Order;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class OrderResponseDto {

    // Getters and Setters
    private UUID id;
    private String orderNumber;
    private UUID userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Order.OrderStatus status;
    private Order.DeliveryType deliveryType;
    private String deliveryAddress;
    private LocalDateTime deliveryDate;
    private String specialInstructions;
    private List<OrderItemResponseDto> items;
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
    public OrderResponseDto() {}

    // Static factory method
    public static OrderResponseDto from(Order order) {
        OrderResponseDto response = new OrderResponseDto();
        response.id = order.getId();
        response.orderNumber = order.getOrderNumber();
        response.userId = order.getUserId();
        response.customerName = order.getCustomerName();
        response.customerEmail = order.getCustomerEmail();
        response.customerPhone = order.getCustomerPhone();
        response.status = order.getStatus();
        response.deliveryType = order.getDeliveryType();
        response.deliveryAddress = order.getDeliveryAddress();
        response.deliveryDate = order.getDeliveryDate();
        response.specialInstructions = order.getSpecialInstructions();
        response.items = order.getOrderItems().stream()
                .map(OrderItemResponseDto::from)
                .collect(Collectors.toList());
        response.subtotal = order.getSubtotal();
        response.taxAmount = order.getTaxAmount();
        response.discountAmount = order.getDiscountAmount();
        response.deliveryFee = order.getDeliveryFee();
        response.totalAmount = order.getTotalAmount();
        response.discountCode = order.getDiscountCode();
        response.discountPercentage = order.getDiscountPercentage();
        response.estimatedPreparationMinutes = order.getEstimatedPreparationMinutes();
        response.estimatedReadyTime = order.getEstimatedReadyTime();
        response.createdAt = order.getCreatedAt();
        response.updatedAt = order.getUpdatedAt();
        response.confirmedAt = order.getConfirmedAt();
        response.completedAt = order.getCompletedAt();
        response.cancelledAt = order.getCancelledAt();
        response.cancellationReason = order.getCancellationReason();
        response.totalItems = order.getTotalItems();
        response.canBeCancelled = order.canBeCancelled();
        response.canBeModified = order.canBeModified();
        return response;
    }

}
