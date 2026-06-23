# bakery_order_service API Report

## HealthController

### `GET` `/api/health`
- **API Name:** health
- **Type:** REST / Synchronous

**Request:**
None

**Response:**
```json
{
  "status": "String - UP",
  "service": "String - bakery-order-service",
  "timestamp": "DateTime",
  "version": "String - 1.0.0",
  "database": "String - UP or DOWN",
  "databaseUrl": "String",
  "databaseError": "String"
}
```

---

### `GET` `/api/info`
- **API Name:** info
- **Type:** REST / Synchronous

**Request:**
None

**Response:**
```json
{
  "serviceName": "String",
  "description": "String",
  "version": "String",
  "features": {},
  "endpoints": {}
}
```

---

### `GET` `/api/metrics`
- **API Name:** metrics
- **Type:** REST / Synchronous

**Request:**
None

**Response:**
```json
{
  "uptime": "String",
  "timestamp": "DateTime",
  "memory": {
    "maxMemory": "String",
    "totalMemory": "String",
    "freeMemory": "String",
    "usedMemory": "String"
  }
}
```

---

## OrderController

### `POST` `/api/orders`
- **API Name:** createOrder
- **Type:** REST / Synchronous
- **Request Headers:**
  - `X-User-Id` (String, optional)
  - `X-User-Role` (String, optional)

**Request:**
```json
{
  "userId": "UUID - Required",
  "customerName": "String - Required (Max 200 chars)",
  "customerEmail": "String - Required (Email format)",
  "customerPhone": "String",
  "deliveryType": "String - Required (e.g. DELIVERY, PICKUP)",
  "deliveryAddress": "String",
  "deliveryDate": "DateTime",
  "specialInstructions": "String (Max 1000 chars)",
  "items": [
    {
      "productId": "UUID - Required",
      "quantity": "Integer - Required (Min 1, Max 100)",
      "specialInstructions": "String",
      "unitPriceOverride": "BigDecimal"
    }
  ],
  "discountCode": "String",
  "paymentMethod": "String - Required (CASH, CARD, DIGITAL_WALLET, etc.)",
  "paymentAmount": "BigDecimal - Required (Min 0.01)",
  "currencyCode": "String - Default 'USD'",
  "cardLastFour": "String",
  "cardBrand": "String",
  "cardType": "String",
  "digitalWalletProvider": "String",
  "bankName": "String",
  "paymentNotes": "String"
}
```

**Response:**
```json
{
  "id": "UUID",
  "orderNumber": "String",
  "userId": "UUID",
  "customerName": "String",
  "customerEmail": "String",
  "customerPhone": "String",
  "status": "String (e.g. PENDING, CONFIRMED, CANCELLED)",
  "deliveryType": "String",
  "deliveryAddress": "String",
  "deliveryDate": "DateTime",
  "specialInstructions": "String",
  "items": [
    {
      "id": "UUID",
      "productId": "UUID",
      "productSku": "String",
      "productName": "String",
      "productCategory": "String",
      "productDescription": "String",
      "productImageUrl": "String",
      "quantity": "Integer",
      "unitPrice": "BigDecimal",
      "discountPerItem": "BigDecimal",
      "effectiveUnitPrice": "BigDecimal",
      "subtotal": "BigDecimal",
      "specialInstructions": "String",
      "preparationTimeMinutes": "Integer",
      "totalPreparationTime": "Integer",
      "hasDiscount": "Boolean",
      "createdAt": "DateTime"
    }
  ],
  "subtotal": "BigDecimal",
  "taxAmount": "BigDecimal",
  "discountAmount": "BigDecimal",
  "deliveryFee": "BigDecimal",
  "totalAmount": "BigDecimal",
  "discountCode": "String",
  "discountPercentage": "BigDecimal",
  "estimatedPreparationMinutes": "Integer",
  "estimatedReadyTime": "DateTime",
  "createdAt": "DateTime",
  "updatedAt": "DateTime",
  "confirmedAt": "DateTime",
  "completedAt": "DateTime",
  "cancelledAt": "DateTime",
  "cancellationReason": "String",
  "totalItems": "Integer",
  "canBeCancelled": "Boolean",
  "canBeModified": "Boolean"
}
```

---

### `GET` `/api/orders`
- **API Name:** getAllOrders
- **Type:** REST / Synchronous
- **Query Parameters:** `page` (int), `size` (int), `sortBy` (String), `sortDir` (String)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Page of OrderResponse)*

---

### `GET` `/api/orders/{orderId}`
- **API Name:** getOrderById
- **Type:** REST / Synchronous
- **Path Variable:** `orderId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Same as `createOrder` OrderResponse)*

---

### `GET` `/api/orders/number/{orderNumber}`
- **API Name:** getOrderByOrderNumber
- **Type:** REST / Synchronous
- **Path Variable:** `orderNumber` (String)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Same as `createOrder` OrderResponse)*

---

### `GET` `/api/orders/user/{userId}`
- **API Name:** getOrdersByUserId
- **Type:** REST / Synchronous
- **Path Variable:** `userId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of OrderResponse)*

---

### `GET` `/api/orders/user/{userId}/paginated`
- **API Name:** getOrdersByUserIdWithPagination
- **Type:** REST / Synchronous
- **Path Variable:** `userId` (UUID)
- **Query Parameters:** `page` (int), `size` (int), `sortBy` (String), `sortDir` (String)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Page of OrderResponse)*

---

### `GET` `/api/orders/status/{status}`
- **API Name:** getOrdersByStatus
- **Type:** REST / Synchronous
- **Path Variable:** `status` (String)

**Request:**
None

**Response:**
*(List of OrderResponse)*

---

### `GET` `/api/orders/search`
- **API Name:** searchOrders
- **Type:** REST / Synchronous
- **Query Parameters:** `query` (String)

**Request:**
None

**Response:**
*(List of OrderResponse)*

---

### `GET` `/api/orders/recent`
- **API Name:** getRecentOrders
- **Type:** REST / Synchronous
- **Query Parameters:** `days` (int, default 7)

**Request:**
None

**Response:**
*(List of OrderResponse)*

---

### `GET` `/api/orders/filter`
- **API Name:** getOrdersWithFilters
- **Type:** REST / Synchronous
- **Query Parameters:** `userId` (UUID), `status` (String), `deliveryType` (String), `paymentMethod` (String), `minAmount` (BigDecimal), `maxAmount` (BigDecimal), `startDate` (DateTime), `endDate` (DateTime)

**Request:**
None

**Response:**
*(List of OrderResponse)*

---

### `PATCH` `/api/orders/{orderId}/status`
- **API Name:** updateOrderStatus
- **Type:** REST / Synchronous
- **Path Variable:** `orderId` (UUID)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
```json
{
  "status": "String - Required (e.g. PENDING, CONFIRMED, COMPLETED)",
  "notes": "String",
  "reason": "String - For cancellations"
}
```

**Response:**
*(Same as `createOrder` OrderResponse)*

---

### `POST` `/api/orders/{orderId}/cancel`
- **API Name:** cancelOrder
- **Type:** REST / Synchronous
- **Path Variable:** `orderId` (UUID)
- **Request Headers:** `X-User-Id` (optional), `X-User-Role` (optional)

**Request:**
```json
{
  "reason": "String"
}
```

**Response:**
*(Same as `createOrder` OrderResponse)*

---

### `GET` `/api/orders/statistics`
- **API Name:** getOrderStatistics
- **Type:** REST / Synchronous
- **Query Parameters:** `startDate` (DateTime), `endDate` (DateTime)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
```json
{
  "totalOrders": "Integer",
  "totalRevenue": "BigDecimal",
  "..." : "..."
}
```
*(Inferred Map response)*

---

### `GET` `/api/orders/health`
- **API Name:** health
- **Type:** REST / Synchronous

**Request:**
None

**Response:**
```json
{
  "status": "String - UP",
  "service": "String - order-service-orders",
  "timestamp": "DateTime"
}
```

---

### `POST` `/api/orders/{orderId}/payment-update`
- **API Name:** updateOrderPaymentStatus
- **Type:** REST / Synchronous
- **Path Variable:** `orderId` (UUID)

**Request:**
```json
{
  "status": "String (COMPLETED, FAILED, CANCELLED)",
  "gatewayResponse": "String"
}
```

**Response:**
```json
{
  "status": "String - updated or acknowledged"
}
```
