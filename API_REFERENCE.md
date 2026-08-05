# API Reference

This document provides a comprehensive overview of all API endpoints exposed by the Bakery Order Service.

## Data Models

### OrderResponse Example
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "orderNumber": "ORD-202308-0001",
  "userId": "123e4567-e89b-12d3-a456-426614174001",
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "customerPhone": "+1234567890",
  "status": "PENDING",
  "deliveryType": "DELIVERY",
  "deliveryAddress": "123 Baker Street",
  "deliveryDate": "2023-08-06T10:00:00",
  "specialInstructions": "Please leave at the door",
  "items": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174002",
      "productId": "123e4567-e89b-12d3-a456-426614174003",
      "productSku": "CROISSANT-01",
      "productName": "Butter Croissant",
      "productCategory": "Pastry",
      "productDescription": "Flaky butter croissant",
      "productImageUrl": "http://example.com/croissant.jpg",
      "quantity": 2,
      "unitPrice": 4.50,
      "discountPerItem": 0.00,
      "effectiveUnitPrice": 4.50,
      "subtotal": 9.00,
      "specialInstructions": "Extra crispy",
      "preparationTimeMinutes": 10,
      "totalPreparationTime": 20,
      "hasDiscount": false,
      "createdAt": "2023-08-05T09:00:00"
    }
  ],
  "subtotal": 9.00,
  "taxAmount": 0.90,
  "discountAmount": 0.00,
  "deliveryFee": 5.00,
  "totalAmount": 14.90,
  "discountCode": null,
  "discountPercentage": null,
  "estimatedPreparationMinutes": 20,
  "estimatedReadyTime": "2023-08-05T09:20:00",
  "createdAt": "2023-08-05T09:00:00",
  "updatedAt": "2023-08-05T09:00:00",
  "confirmedAt": null,
  "completedAt": null,
  "cancelledAt": null,
  "cancellationReason": null,
  "totalItems": 2,
  "canBeCancelled": true,
  "canBeModified": true,
  "paymentMethod": "CARD",
  "paymentStatus": "PENDING"
}
```

### PagedModel Example (Array of Orders)
Endpoints returning multiple results use pagination metadata.
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "orderNumber": "ORD-202308-0001",
      "status": "PENDING"
      // ... full OrderResponse object
    }
  ],
  "page": {
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "number": 0
  }
}
```

---

## Order Command Controller
Source: [`OrderCommandController`](./src/main/java/com/blubugtech/bakery_order_service/controller/OrderCommandController.java)

### 1. Create Order
Creates a new order.

**Endpoint:** `POST /api/orders`

**Request Headers:**
- `X-User-Id` (Optional): UUID of the user

**Request Body:**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174001",
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "customerPhone": "+1234567890",
  "deliveryType": "DELIVERY",
  "deliveryAddress": "123 Baker Street",
  "deliveryDate": "2023-08-06T10:00:00",
  "specialInstructions": "Please leave at the door",
  "items": [
    {
      "productId": "123e4567-e89b-12d3-a456-426614174003",
      "quantity": 2,
      "specialInstructions": "Extra crispy",
      "unitPriceOverride": null
    }
  ],
  "discountCode": "WELCOME10",
  "currencyCode": "INR",
  "discountAmount": 0.00,
  "taxAmount": 0.90,
  "paymentMethod": "CARD",
  "paymentAmount": 14.90,
  "cardLastFour": "4242",
  "cardBrand": "Visa",
  "cardType": "Credit",
  "digitalWalletProvider": null,
  "bankName": null,
  "paymentNotes": "Paid via gateway"
}
```

**Response Body:** `OrderResponse` (See [OrderResponse Example](#orderresponse-example))

### 2. Update Order Status
Updates the status of an existing order. Requires `ADMIN` or `BAKER` role.

**Endpoint:** `PATCH /api/orders/{orderId}/status`

**Request Body:**
```json
{
  "status": "CONFIRMED",
  "notes": "Order confirmed and being prepared",
  "reason": null
}
```

**Response Body:** `OrderResponse` (See [OrderResponse Example](#orderresponse-example))

### 3. Cancel Order
Cancels an order (user request).

**Endpoint:** `POST /api/orders/{orderId}/cancel`

**Query Parameters:**
- `reason` (Optional): String

**Request Body (Optional):**
```json
{
  "reason": "Changed my mind"
}
```

**Response Body:** `OrderResponse` (See [OrderResponse Example](#orderresponse-example))

### 4. Update Payment Status
Webhook or internal call to update the payment status of an order.

**Endpoint:** `POST /api/orders/{orderId}/payment-update`

**Request Body:**
```json
{
  "status": "PAID",
  "gatewayResponse": "txn_123456789"
}
```

**Response Body:**
```json
{
  "message": "Payment status updated"
}
```

---

## Order Query Controller
Source: [`OrderQueryController`](./src/main/java/com/blubugtech/bakery_order_service/controller/OrderQueryController.java)

*All standard GET endpoints below return the full `OrderResponse` model or a `PagedModel` of them.*

### 1. Get Order By ID
**Endpoint:** `GET /api/orders/{orderId}`  
**Headers:** `X-User-Id` (Optional)  
**Response Body:** `OrderResponse`

### 2. Get Order By Number
**Endpoint:** `GET /api/orders/number/{orderNumber}`  
**Response Body:** `OrderResponse`

### 3. Get Orders By User ID
**Endpoint:** `GET /api/orders/user/{userId}`  
**Query Parameters:** `page`, `size`, `sortBy`, `sortDir`  
**Response Body:** `PagedModel` of `OrderResponse`

### 4. Get Active Orders By User ID
**Endpoint:** `GET /api/orders/user/{userId}/active`  
**Query Parameters:** `page`, `size`, `sortBy`, `sortDir`  
**Response Body:** `PagedModel` of `OrderResponse`

### 5. Get Orders By User ID With Pagination
**Endpoint:** `GET /api/orders/user/{userId}/paginated`  
**Query Parameters:** `page`, `size`, `sortBy`, `sortDir`  
**Response Body:** `PagedModel` of `OrderResponse`

### 6. Get Orders By Status
**Endpoint:** `GET /api/orders/status/{status}`  
**Query Parameters:** `page`, `size`, `sortBy`, `sortDir`  
**Response Body:** `PagedModel` of `OrderResponse`

### 7. Search Orders
**Endpoint:** `GET /api/orders/search`  
**Query Parameters:** `query`, `page`, `size`, `sortBy`, `sortDir`  
**Response Body:** `PagedModel` of `OrderResponse`

### 8. Get Recent Orders
**Endpoint:** `GET /api/orders/recent`  
**Query Parameters:** `days`, `page`, `size`, `sortBy`, `sortDir`  
**Response Body:** `PagedModel` of `OrderResponse`

### 9. Filter Orders
**Endpoint:** `GET /api/orders/filter`  
**Query Parameters:** `userId`, `status`, `deliveryType`, `paymentMethod`, `minAmount`, `maxAmount`, `startDate`, `endDate`, `page`, `size`, `sortBy`, `sortDir`  
**Response Body:** `PagedModel` of `OrderResponse`

---

## Order Admin Controller
Source: [`OrderAdminController`](./src/main/java/com/blubugtech/bakery_order_service/controller/OrderAdminController.java)

*Requires `ADMIN` role for all endpoints.*

### 1. Get All Orders
**Endpoint:** `GET /api/orders`  
**Query Parameters:** `page`, `size`, `sortBy`, `sortDir`  
**Response Body:** `PagedModel` of `OrderResponse`

### 2. Search Orders (Admin)
**Endpoint:** `GET /api/orders/admin/search`  
**Query Parameters:** `query`, `page`, `size`, `sortBy`, `sortDir`  
**Response Body:** `PagedModel` of `OrderResponse`

### 3. Admin Cancel Order
Forces order cancellation, usually overriding user constraints.

**Endpoint:** `POST /api/orders/{orderId}/admin-cancel`

**Query Parameters:**
- `reason` (Optional): String

**Request Body (Optional):**
```json
{
  "reason": "Out of stock"
}
```

**Response Body:** `OrderResponse` (See [OrderResponse Example](#orderresponse-example))

### 4. Get Order Statistics
Fetches system-wide order statistics.

**Endpoint:** `GET /api/orders/statistics`

**Query Parameters:**
- `startDate` (Optional, ISO Date-Time)
- `endDate` (Optional, ISO Date-Time)

**Response Body:**
```json
{
  "totalOrders": 1500,
  "totalRevenue": 45500.50,
  "averageOrderValue": 30.33,
  "pendingOrders": 15,
  "completedOrders": 1400,
  "cancelledOrders": 85,
  "dateRange": {
    "startDate": "2023-07-01T00:00:00",
    "endDate": "2023-08-01T23:59:59"
  }
}
```
