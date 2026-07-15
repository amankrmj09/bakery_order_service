# Bakery Order Service API Reference

This document provides a detailed overview of the REST APIs exposed by the bakery_order_service.

---

## Health Controller
**Base Path:** `/api`

### 1. Main Service Health Check
- **Method:** `GET`
- **Path:** `/api/health`
- **Type of API:** `Public`
- **Request Body:** None
- **Response Body:** `200 OK`
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
- **Method:** `GET`
- **Path:** `/api/info`
- **Type of API:** `Public`
- **Request Body:** None
- **Response Body:** `200 OK`
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
- **Method:** `GET`
- **Path:** `/api/metrics`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
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
**Base Path:** `/api/orders`

### 1. Create New Order
- **Method:** `POST`
- **Path:** `/api/orders`
- **Type of API:** `User`
- **Request Body:**
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
- **Response Body:** `201 Created`
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
- **Method:** `GET`
- **Path:** `/api/orders`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
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
- **Method:** `GET`
- **Path:** `/api/orders/{orderId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  *(Returns a single `OrderResponseDto`)*

### 4. Get Order by Order Number
- **Method:** `GET`
- **Path:** `/api/orders/number/{orderNumber}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  *(Returns a single `OrderResponseDto`)*

### 5. Get Orders by User ID
- **Method:** `GET`
- **Path:** `/api/orders/user/{userId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
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
- **Method:** `GET`
- **Path:** `/api/orders/user/{userId}/paginated`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  *(Returns a paginated response of `OrderResponseDto` objects)*

### 7. Get Orders by Status
- **Method:** `GET`
- **Path:** `/api/orders/status/{status}`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  *(Returns a List of `OrderResponseDto` objects)*

### 8. Search Orders
- **Method:** `GET`
- **Path:** `/api/orders/search`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  *(Returns a List of `OrderResponseDto` objects)*

### 9. Get Recent Orders
- **Method:** `GET`
- **Path:** `/api/orders/recent`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  *(Returns a List of `OrderResponseDto` objects)*

### 10. Advanced Filter Search
- **Method:** `GET`
- **Path:** `/api/orders/filter`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  *(Returns a List of `OrderResponseDto` objects)*

### 11. Update Order Status
- **Method:** `PATCH`
- **Path:** `/api/orders/{orderId}/status`
- **Type of API:** `Admin`
- **Request Body:**
  ```json
  {
    "status": "CONFIRMED",
    "notes": "Order is confirmed and being prepared",
    "reason": null
  }
  ```
- **Response Body:** `200 OK`
  *(Returns the updated `OrderResponseDto`)*

### 12. Cancel Order
- **Method:** `POST`
- **Path:** `/api/orders/{orderId}/cancel`
- **Type of API:** `User`
- **Request Body:**
  ```json
  {
    "reason": "Customer requested cancellation"
  }
  ```
- **Response Body:** `200 OK`
  *(Returns the updated `OrderResponseDto` with status `CANCELLED`)*

### 13. Get Order Statistics
- **Method:** `GET`
- **Path:** `/api/orders/statistics`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
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

### 14. Orders Health Check
- **Method:** `GET`
- **Path:** `/api/orders/health`
- **Type of API:** `Public`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "status": "UP",
    "service": "order-service-orders",
    "timestamp": "2026-07-13T10:00:00"
  }
  ```

### 15. Payment Status Update Webhook
- **Method:** `POST`
- **Path:** `/api/orders/{orderId}/payment-update`
- **Type of API:** `Public`
- **Request Body:**
  ```json
  {
    "status": "COMPLETED",
    "gatewayResponse": "Payment processed successfully",
    "transactionId": "TXN-123456"
  }
  ```
- **Response Body:** `200 OK`
  ```json
  {
    "status": "updated"
  }
  ```
