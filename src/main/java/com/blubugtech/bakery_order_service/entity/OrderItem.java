package com.blubugtech.bakery_order_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id"),
    @Index(name = "idx_order_item_product", columnList = "product_id")
})
public class OrderItem {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order is required")
    private Order order;

    @Column(name = "product_id", nullable = false)
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @Column(name = "product_sku", nullable = false, length = 50)
    @NotBlank(message = "Product SKU is required")
    @Size(max = 50, message = "Product SKU must not exceed 50 characters")
    private String productSku;

    @Column(name = "product_name", nullable = false, length = 200)
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String productName;

    @Column(name = "product_category", length = 100)
    @Size(max = 100, message = "Product category must not exceed 100 characters")
    private String productCategory;

    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100 per item")
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Invalid unit price format")
    private BigDecimal unitPrice;

    @Column(name = "discount_per_item", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Discount per item cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid discount format")
    private BigDecimal discountPerItem = BigDecimal.ZERO;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    @Size(max = 500, message = "Special instructions must not exceed 500 characters")
    private String specialInstructions;

    // Product snapshot for historical records
    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "product_image_url")
    private String productImageUrl;

    @Column(name = "preparation_time_minutes")
    @Min(value = 0, message = "Preparation time cannot be negative")
    private Integer preparationTimeMinutes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public OrderItem() {}

    public OrderItem(Order order, UUID productId, String productSku, String productName,
                     Integer quantity, BigDecimal unitPrice) {
        this.order = order;
        this.productId = productId;
        this.productSku = productSku;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Utility Methods
    public BigDecimal getSubtotal() {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (discountPerItem != null && discountPerItem.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalDiscount = discountPerItem.multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.subtract(totalDiscount);
        }
        return subtotal;
    }

    public BigDecimal getEffectiveUnitPrice() {
        return unitPrice.subtract(discountPerItem != null ? discountPerItem : BigDecimal.ZERO);
    }

    public boolean hasDiscount() {
        return discountPerItem != null && discountPerItem.compareTo(BigDecimal.ZERO) > 0;
    }

    public Integer getTotalPreparationTime() {
        return preparationTimeMinutes != null ? preparationTimeMinutes * quantity : 0;
    }
}
