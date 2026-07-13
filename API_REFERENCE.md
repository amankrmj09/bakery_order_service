# Bakery Order Service API Reference

This document provides a detailed overview of the REST APIs exposed by the `bakery_order_service`.

---

## Health Controller
Handles general service health, info, and metrics.

### 1. Main Service Health Check
- **API Name:** `health`
- **Method:** `GET`
- **Path:** `/api/health`
- **Description:** Returns the main health status of the order service.

**Request Body:**
None

**Response (JSON):**
```json
{
  "status": "UP",
  "service": "bakery-order-service",
  "timestamp": "2026-07-13T10:00:00",
  "version": "1.0.0",
  "database": "UP",
  "databaseUrl": "jdbc:mysql://localhost:3306/bakery",
  "databaseError": "null or error message"
}
```

### 2. Service Info
- **API Name:** `info`
- **Method:** `GET`
- **Path:** `/api/info`
- **Description:** Returns information about the service including features and endpoints.

**Request Body:**
None

**Response (JSON):**
```json
{
  "serviceName": "Bakery Order Service",
  "description": "Order management and payment processing service",
  "version": "1.0.0",
  "features": {
    "orders": "Complete order lifecycle management",
    "payments": "Multi-method payment processing",
    "integration": "Product service integration for stock management",
    "analytics": "Order and payment analytics"
  },
  "endpoints": {
    "orders": "/api/orders",
    "payments": "/api/payments"
  }
}
```

### 3. Service Metrics
- **API Name:** `metrics`
- **Method:** `GET`
- **Path:** `/api/metrics`
- **Description:** Returns application metrics like uptime and memory usage.

**Request Body:**
None

**Response (JSON):**
```json
{
  "uptime": "1 days, 2 hours, 30 minutes, 15 seconds",
  "timestamp": "2026-07-13T10:00:00",
  "memory": {
    "maxMemory": "1024 MB",
    "totalMemory": "512 MB",
    "freeMemory": "256 MB",
    "usedMemory": "256 MB"
  }
}
```

---

## Order Controller
Handles all order-related operations.

### 1. Create New Order
- **API Name:** `createOrder`
- **Method:** `POST`
- **Path:** `/api/orders`
- **Headers:** 
  - `X-User-Id` (UUID, optional)
  - `X-User-Role` (String, optional)

**Request Body (JSON):**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "customerPhone": "+1234567890",
  "deliveryType": "PICKUP", 
  "deliveryAddress": "123 Bakery St, City",
  "deliveryDate": "2026-07-14T15:00:00",
  "specialInstructions": "Extra napkins please",
  "items": [
    {
      "productId": "123e4567-e89b-12d3-a456-426614174001",
      "quantity": 2,
      "specialInstructions": "No nuts",
      "unitPriceOverride": 15.50
    }
  ],
  "discountCode": "SUMMER10",
  "paymentMethod": "CARD",
  "paymentAmount": 31.00,
  "currencyCode": "USD",
  "cardLastFour": "1234",
  "cardBrand": "Visa",
  "cardType": "Credit",
  "digitalWalletProvider": "ApplePay",
  "bankName": "Chase",
  "paymentNotes": "Paid online"
}
```

**Response (JSON):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174002",
  "orderNumber": "ORD-20260713-1234",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "customerPhone": "+1234567890",
  "status": "PENDING",
  "deliveryType": "PICKUP",
  "deliveryAddress": "123 Bakery St, City",
  "deliveryDate": "2026-07-14T15:00:00",
  "specialInstructions": "Extra napkins please",
  "items": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174003",
      "productId": "123e4567-e89b-12d3-a456-426614174001",
      "productSku": "SKU-123",
      "productName": "Chocolate Cake",
      "productCategory": "Cakes",
      "productDescription": "Delicious chocolate cake",
      "productImageUrl": "http://example.com/cake.jpg",
      "quantity": 2,
      "unitPrice": 15.50,
      "discountPerItem": 0.00,
      "effectiveUnitPrice": 15.50,
      "subtotal": 31.00,
      "specialInstructions": "No nuts",
      "preparationTimeMinutes": 30,
      "totalPreparationTime": 60,
      "hasDiscount": false,
      "createdAt": "2026-07-13T10:00:00"
    }
  ],
  "subtotal": 31.00,
  "taxAmount": 2.48,
  "discountAmount": 3.10,
  "deliveryFee": 0.00,
  "totalAmount": 30.38,
  "discountCode": "SUMMER10",
  "discountPercentage": 10.0,
  "estimatedPreparationMinutes": 60,
  "estimatedReadyTime": "2026-07-13T11:00:00",
  "createdAt": "2026-07-13T10:00:00",
  "updatedAt": "2026-07-13T10:00:00",
  "confirmedAt": null,
  "completedAt": null,
  "cancelledAt": null,
  "cancellationReason": null,
  "totalItems": 2,
  "canBeCancelled": true,
  "canBeModified": true
}
```

### 2. Get All Orders with Pagination
- **API Name:** `getAllOrders`
- **Method:** `GET`
- **Path:** `/api/orders`
- **Query Params:**
  - `page` (default: 0)
  - `size` (default: 20)
  - `sortBy` (default: createdAt)
  - `sortDir` (default: DESC)
- **Headers:** 
  - `X-User-Role` (String, optional)

**Request Body:**
None

**Response (JSON):**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174002",
      "orderNumber": "ORD-20260713-1234",
      "status": "PENDING"
      // ... full OrderResponseDto
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

### 3. Get Order by ID
- **API Name:** `getOrderById`
- **Method:** `GET`
- **Path:** `/api/orders/{orderId}`
- **Headers:** 
  - `X-User-Id` (UUID, optional)
  - `X-User-Role` (String, optional)

**Request Body:**
None

**Response (JSON):**
Returns a single `OrderResponseDto` (see Create New Order for structure).

### 4. Get Order by Order Number
- **API Name:** `getOrderByOrderNumber`
- **Method:** `GET`
- **Path:** `/api/orders/number/{orderNumber}`
- **Headers:** 
  - `X-User-Id` (UUID, optional)
  - `X-User-Role` (String, optional)

**Request Body:**
None

**Response (JSON):**
Returns a single `OrderResponseDto` (see Create New Order for structure).

### 5. Get Orders by User ID
- **API Name:** `getOrdersByUserId`
- **Method:** `GET`
- **Path:** `/api/orders/user/{userId}`
- **Headers:** 
  - `X-User-Id` (UUID, optional)
  - `X-User-Role` (String, optional)

**Request Body:**
None

**Response (JSON):**
Returns a List of `OrderResponseDto` objects.
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174002",
    "orderNumber": "ORD-20260713-1234"
    // ... full OrderResponseDto
  }
]
```

### 6. Get Orders by User ID with Pagination
- **API Name:** `getOrdersByUserIdWithPagination`
- **Method:** `GET`
- **Path:** `/api/orders/user/{userId}/paginated`
- **Query Params:**
  - `page` (default: 0)
  - `size` (default: 10)
  - `sortBy` (default: createdAt)
  - `sortDir` (default: DESC)
- **Headers:** 
  - `X-User-Id` (UUID, optional)
  - `X-User-Role` (String, optional)

**Request Body:**
None

**Response (JSON):**
Returns a paginated response of `OrderResponseDto` objects (see Get All Orders with Pagination).

### 7. Get Orders by Status
- **API Name:** `getOrdersByStatus`
- **Method:** `GET`
- **Path:** `/api/orders/status/{status}`
- **Description:** Valid statuses: `PENDING`, `CONFIRMED`, `PREPARING`, `READY`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`

**Request Body:**
None

**Response (JSON):**
Returns a List of `OrderResponseDto` objects.

### 8. Search Orders
- **API Name:** `searchOrders`
- **Method:** `GET`
- **Path:** `/api/orders/search`
- **Query Params:**
  - `query` (String)

**Request Body:**
None

**Response (JSON):**
Returns a List of `OrderResponseDto` objects.

### 9. Get Recent Orders
- **API Name:** `getRecentOrders`
- **Method:** `GET`
- **Path:** `/api/orders/recent`
- **Query Params:**
  - `days` (int, default: 7)

**Request Body:**
None

**Response (JSON):**
Returns a List of `OrderResponseDto` objects.

### 10. Advanced Filter Search
- **API Name:** `getOrdersWithFilters`
- **Method:** `GET`
- **Path:** `/api/orders/filter`
- **Query Params:**
  - `userId` (UUID, optional)
  - `status` (Order.OrderStatus, optional)
  - `deliveryType` (Order.DeliveryType, optional)
  - `paymentMethod` (String, optional)
  - `minAmount` (BigDecimal, optional)
  - `maxAmount` (BigDecimal, optional)
  - `startDate` (ISO DateTime, optional)
  - `endDate` (ISO DateTime, optional)

**Request Body:**
None

**Response (JSON):**
Returns a List of `OrderResponseDto` objects.

### 11. Update Order Status
- **API Name:** `updateOrderStatus`
- **Method:** `PATCH`
- **Path:** `/api/orders/{orderId}/status`
- **Headers:** 
  - `X-User-Role` (String, optional)

**Request Body (JSON):**
```json
{
  "status": "CONFIRMED",
  "notes": "Order is confirmed and being prepared",
  "reason": null
}
```

**Response (JSON):**
Returns the updated `OrderResponseDto`.

### 12. Cancel Order
- **API Name:** `cancelOrder`
- **Method:** `POST`
- **Path:** `/api/orders/{orderId}/cancel`
- **Headers:** 
  - `X-User-Id` (UUID, optional)
  - `X-User-Role` (String, optional)

**Request Body (JSON):**
```json
{
  "reason": "Customer requested cancellation"
}
```

**Response (JSON):**
Returns the updated `OrderResponseDto` with status `CANCELLED`.

### 13. Get Order Statistics
- **API Name:** `getOrderStatistics`
- **Method:** `GET`
- **Path:** `/api/orders/statistics`
- **Query Params:**
  - `startDate` (ISO DateTime, optional)
  - `endDate` (ISO DateTime, optional)
- **Headers:** 
  - `X-User-Role` (String, optional)

**Request Body:**
None

**Response (JSON):**
```json
{
  "totalOrders": 150,
  "totalRevenue": 4500.50,
  "averageOrderValue": 30.00,
  "statusCounts": {
    "DELIVERED": 120,
    "CANCELLED": 10,
    "PENDING": 20
  }
}
```
*(Exact fields depend on the internal map returned by the service)*

### 14. Orders Health Check
- **API Name:** `health`
- **Method:** `GET`
- **Path:** `/api/orders/health`

**Request Body:**
None

**Response (JSON):**
```json
{
  "status": "UP",
  "service": "order-service-orders",
  "timestamp": "2026-07-13T10:00:00"
}
```

### 15. Payment Status Update Webhook
- **API Name:** `updateOrderPaymentStatus`
- **Method:** `POST`
- **Path:** `/api/orders/{orderId}/payment-update`

**Request Body (JSON):**
```json
{
  "status": "COMPLETED",
  "gatewayResponse": "Payment processed successfully",
  "transactionId": "TXN-123456"
}
```

**Response (JSON):**
```json
{
  "status": "updated"
}
```
*(Returns "acknowledged" if it fails to update but accepts the webhook call to prevent retries)*
