package com.blubugtech.bakery_order_service.repository;

import com.blubugtech.bakery_order_service.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    // Find order items by order ID
    List<OrderItem> findByOrderIdOrderByCreatedAt(UUID orderId);

    // Find order items by product ID
    List<OrderItem> findByProductIdOrderByCreatedAtDesc(UUID productId);

    // Find order items by product SKU
    List<OrderItem> findByProductSkuOrderByCreatedAtDesc(String productSku);

    // Count order items by product ID
    long countByProductId(UUID productId);

    // Get total quantity sold for a product
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.productId = :productId")
    Integer getTotalQuantitySoldForProduct(@Param("productId") UUID productId);

    // Get total quantity sold for a product in date range
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
           "WHERE oi.productId = :productId " +
           "AND oi.createdAt BETWEEN :startDate AND :endDate")
    Integer getTotalQuantitySoldForProductInRange(@Param("productId") UUID productId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Get total revenue for a product
    @Query("SELECT COALESCE(SUM(oi.unitPrice * oi.quantity), 0) FROM OrderItem oi WHERE oi.productId = :productId")
    BigDecimal getTotalRevenueForProduct(@Param("productId") UUID productId);

    // Get best-selling products
    @Query("SELECT oi.productId as productId, " +
           "oi.productSku as productSku, " +
           "oi.productName as productName, " +
           "SUM(oi.quantity) as totalQuantity, " +
           "COUNT(DISTINCT oi.order.id) as orderCount, " +
           "SUM(oi.unitPrice * oi.quantity) as totalRevenue " +
           "FROM OrderItem oi " +
           "WHERE oi.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.productId, oi.productSku, oi.productName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getBestSellingProducts(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // Get products by category sales
    @Query("SELECT oi.productCategory as category, " +
           "SUM(oi.quantity) as totalQuantity, " +
           "COUNT(DISTINCT oi.productId) as productCount, " +
           "SUM(oi.unitPrice * oi.quantity) as totalRevenue " +
           "FROM OrderItem oi " +
           "WHERE oi.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.productCategory " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getSalesByCategory(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    // Get order items with special instructions
    @Query("SELECT oi FROM OrderItem oi " +
           "WHERE oi.specialInstructions IS NOT NULL " +
           "AND oi.specialInstructions != '' " +
           "ORDER BY oi.createdAt DESC")
    List<OrderItem> findItemsWithSpecialInstructions();

    // Get average order item quantity
    @Query("SELECT AVG(oi.quantity) FROM OrderItem oi " +
           "WHERE oi.createdAt BETWEEN :startDate AND :endDate")
    Double getAverageQuantityPerItem(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    // Get products that haven't been ordered recently
    @Query("SELECT DISTINCT oi.productId FROM OrderItem oi " +
           "WHERE oi.productId NOT IN (" +
           "    SELECT oi2.productId FROM OrderItem oi2 " +
           "    WHERE oi2.createdAt >= :cutoffDate" +
           ")")
    List<UUID> getProductsNotOrderedSince(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Get order items by price range
    List<OrderItem> findByUnitPriceBetweenOrderByCreatedAtDesc(BigDecimal minPrice, BigDecimal maxPrice);

    // Get order items with discounts
    @Query("SELECT oi FROM OrderItem oi " +
           "WHERE oi.discountPerItem > 0 " +
           "ORDER BY oi.createdAt DESC")
    List<OrderItem> findItemsWithDiscount();

    // Get total preparation time for an order
    @Query("SELECT COALESCE(SUM(oi.preparationTimeMinutes * oi.quantity), 0) " +
           "FROM OrderItem oi WHERE oi.order.id = :orderId")
    Integer getTotalPreparationTimeForOrder(@Param("orderId") UUID orderId);

    // Find frequently ordered together products (basic version)
    @Query("SELECT oi1.productId as product1, oi2.productId as product2, COUNT(*) as frequency " +
           "FROM OrderItem oi1 " +
           "JOIN OrderItem oi2 ON oi1.order.id = oi2.order.id " +
           "WHERE oi1.productId < oi2.productId " +
           "GROUP BY oi1.productId, oi2.productId " +
           "HAVING COUNT(*) > :minFrequency " +
           "ORDER BY COUNT(*) DESC")
    List<Object[]> findFrequentlyOrderedTogether(@Param("minFrequency") Long minFrequency);

    // Get order item statistics
    @Query("SELECT " +
           "COUNT(oi) as totalItems, " +
           "SUM(oi.quantity) as totalQuantity, " +
           "AVG(oi.quantity) as averageQuantity, " +
           "AVG(oi.unitPrice) as averagePrice, " +
           "COUNT(DISTINCT oi.productId) as uniqueProducts " +
           "FROM OrderItem oi " +
           "WHERE oi.createdAt BETWEEN :startDate AND :endDate")
    Object[] getOrderItemStatistics(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);
}
