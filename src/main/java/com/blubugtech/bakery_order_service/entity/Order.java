package com.blubugtech.bakery_order_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_date", columnList = "created_at"),
    @Index(name = "idx_order_number", columnList = "order_number"),
    @Index(name = "idx_order_delivery_date", columnList = "delivery_date")
})
public class Order {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_number", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Column(name = "customer_name", nullable = false, length = 200)
    @NotBlank(message = "Customer name is required")
    @Size(max = 200, message = "Customer name must not exceed 200 characters")
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 255)
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String customerEmail;

    @Column(name = "customer_phone", length = 20)
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String customerPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false)
    private DeliveryType deliveryType = DeliveryType.PICKUP;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Special instructions must not exceed 1000 characters")
    private String specialInstructions;

    // Order Items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<OrderItem> orderItems = new ArrayList<>();

    // Pricing Information
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.00", message = "Subtotal cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid subtotal format")
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Tax amount is required")
    @DecimalMin(value = "0.00", message = "Tax amount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid tax amount format")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Discount amount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid discount amount format")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Delivery fee cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid delivery fee format")
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Invalid total amount format")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Discount Information
    @Column(name = "discount_code", length = 50)
    @Size(max = 50, message = "Discount code must not exceed 50 characters")
    private String discountCode;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Discount percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Discount percentage cannot exceed 100%")
    private BigDecimal discountPercentage;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // Estimated preparation and delivery times
    @Column(name = "estimated_preparation_minutes")
    @Min(value = 0, message = "Estimated preparation time cannot be negative")
    private Integer estimatedPreparationMinutes;

    @Column(name = "estimated_ready_time")
    private LocalDateTime estimatedReadyTime;

    // Constructors
    public Order() {}

    public Order(UUID userId, String customerName, String customerEmail, DeliveryType deliveryType) {
        this.userId = userId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.deliveryType = deliveryType;
        this.orderNumber = generateOrderNumber();
    }

    // Utility Methods
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }

    public int getTotalItems() {
        return orderItems.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public boolean canBeModified() {
        return status == OrderStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    public void calculateTotals(BigDecimal taxRate) {
        // Calculate subtotal from items
        this.subtotal = orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // Apply discount if any
        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal discountedSubtotal = subtotal.subtract(discount).setScale(2, java.math.RoundingMode.HALF_UP);

        // Calculate tax
        this.taxAmount = discountedSubtotal.multiply(taxRate)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // Calculate total
        BigDecimal delivery = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
        this.totalAmount = discountedSubtotal.add(taxAmount).add(delivery)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String generateOrderNumber() {
        // Generate order number: ORD-YYYYMMDD-XXXX
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.valueOf((int) (Math.random() * 9000) + 1000);
        return "ORD-" + timestamp + "-" + randomPart;
    }

    // Enums
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PREPARING,
        READY,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELLED
    }

    public enum DeliveryType {
        PICKUP,
        DELIVERY
    }
}
