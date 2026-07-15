# 🧁 Order Service

![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)

Welcome to the **Order Service**, a core component of the Shah's Bakery Microservice Platform.

## 📑 Table of Contents
- [Features](#-features)
- [Folder Structure](#-folder-structure)
- [Dependencies](#-dependencies)
- [Endpoints](#-endpoints)
- [How to Run](#-how-to-run)
- [Related Links](#-related-links)

## ✨ Features
- Comprehensive order lifecycle management.
- Integration with payment gateways via asynchronous events.
- Inter-service communication with Product and Cart services.

## 📁 Folder Structure
The main `src/main/java` directory is organized as follows:
```text
src/
└── main/
    └── java/.../bakery_order_service/
        ├── client/     # Feign clients for communicating with Product and Auth services.
        ├── config/     # General service configurations.
        ├── controller/ # REST endpoints for order lifecycle management.
        ├── dto/        # Data Transfer Objects for orders.
        ├── entity/     # Database entities mapping to PostgreSQL.
        ├── exception/  # Custom exceptions like InsufficientStockException.
        ├── kafka/      # Event consumers for handling asynchronous payment updates.
        ├── repository/ # Spring Data JPA interfaces.
        └── service/    # Core logic for order processing and Kafka event publishing.
```

## 🛠️ Dependencies
- **Framework:** Spring Boot
- **Database:** PostgreSQL
- **Key Modules:** Eureka Client, Spring Data JPA, OpenFeign

## 🌐 Endpoints
> [!NOTE]
> For complete and detailed API definitions, please refer to the OpenAPI Reference available via the API Gateway's Swagger UI.

- `POST /api/orders` - Creates a new order from a user's cart.
- `GET /api/orders/{id}` - Retrieves details and status of a specific order.
- `PUT /api/orders/{id}/status` - Updates the fulfillment status of an order.
- `GET /api/orders/user/{userId}` - Retrieves all orders for a specific user.

## 🚀 How to Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/amankrmj01/bakery_order_service.git
   cd bakery_order_service
   ```

2. **Configure Environment:**
   Ensure your `.env` or `application.yml` properties (including DB credentials) are set.

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

## 🔗 Related Links
- [Main Platform README](../README.md)