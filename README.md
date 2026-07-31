# 🚀 Order Service

![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)
![Database](https://img.shields.io/badge/Database-PostgreSQL-blue.svg)

Order Management and Processing Service for Bakery. It is a core component of the Shah's Bakery Microservice Platform that handles comprehensive order lifecycle management, integrates with payment gateways via asynchronous events, and handles inter-service communication with Product and Cart services.

## 📑 Table of Contents
- [Architecture & Design](#-architecture--design)
- [Features](#-features)
- [Folder Structure](#-folder-structure)
- [API Reference](#-api-reference)
- [Configuration](#-configuration)
- [How to Run Locally](#-how-to-run-locally)
- [Testing](#-testing)
- [Dependencies](#-dependencies)
- [Related Links](#-related-links)

## 🏗️ Architecture & Design
Provide a brief overview of the architecture of this service.
- **Data Storage**: PostgreSQL for relational data
- **Communication**: REST API for synchronous communication, Kafka for async events (payment updates), OpenFeign clients for inter-service communication (Product and Auth services)
- **Key Design Patterns**: MVC, Repository Pattern, DTO pattern, Event-Driven Architecture (Kafka consumers)

## ✨ Features
List the core capabilities and features of this service.
- Comprehensive order lifecycle management.
- Integration with payment gateways via asynchronous Kafka events.
- Inter-service communication with Product and Cart services for cart validation and product updates.

## 📁 Folder Structure
The source code under `src/main/java` is organized as follows:
```text
src/
└── main/
    └── java/.../bakery_order_service/
        ├── client/     # Feign clients for communicating with Product and Auth services
        ├── config/     # Spring Boot configurations (Security, Beans, etc.)
        ├── controller/ # REST endpoints for order lifecycle management
        ├── dto/        # Data Transfer Objects for orders
        ├── entity/     # Database entities mapping to PostgreSQL
        ├── exception/  # Custom exceptions like InsufficientStockException and global exception handler
        ├── kafka/      # Event consumers for handling asynchronous payment updates
        ├── repository/ # Spring Data JPA interfaces
        └── service/    # Core logic for order processing and Kafka event publishing
```

## 🌐 API Reference
> [!NOTE]
> For complete and detailed API definitions, request/response bodies, and schemas, please refer to the OpenAPI Reference available via the API Gateway's Swagger UI.

**Key Endpoints:**
- `POST /api/orders` - Creates a new order from a user's cart.
- `GET /api/orders/{id}` - Retrieves details and status of a specific order.
- `PUT /api/orders/{id}/status` - Updates the fulfillment status of an order.
- `GET /api/orders/user/{userId}` - Retrieves all orders for a specific user.

## ⚙️ Configuration
List required environment variables and configurations.
You can copy `.env.example` to `.env` and fill in the values.

| Variable | Description | Default / Example |
|----------|-------------|-------------------|
| `ACTIVE_PROFILE` | Active Spring profile | `dev` |
| `CONFIG_SERVER_URL` | URL for Spring Cloud Config Server | `http://localhost:8888` |
| `EUREKA_URL` | Eureka server URL | `http://localhost:8761/eureka/` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `localhost:9092` |
| `ORDER_DB_URL` | Database connection URL | `jdbc:postgresql://localhost:5432/bakery_order` |
| `ORDER_DB_USER` | Database user | `postgres` |
| `ORDER_DB_PASSWORD` | Database password | `password` |
| `SERVER_PORT` | Port for the service | `8080` |
| `ORDER_DELIVERY_TIME` | Order delivery time configuration | |
| `ORDER_MAX_ITEMS` | Maximum items per order | |
| `ORDER_MAX_VALUE` | Maximum value per order | |

## 🚀 How to Run Locally

### Prerequisites
- JDK 21+
- Gradle
- PostgreSQL
- Kafka
- Eureka Server and Config Server (optional, depending on setup)

### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/amankrmj01/bakery_order_service.git
   cd bakery_order_service
   ```

2. **Configure Environment:**
   Set up your `.env` file based on `.env.example`. Make sure backing services (like PostgreSQL and Kafka) are running.

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

## 🧪 Testing
To run the test suite:
```bash
./gradlew test
```

## 🛠️ Dependencies
- **Framework:** Spring Boot 3.5.x
- **Database:** PostgreSQL
- **Key Modules:** Spring Web, Spring Data JPA, Eureka Client, Spring Kafka, OpenFeign, Spring Security
- **Other Utilities:** Flyway, MapStruct, Lombok, Springdoc OpenAPI

## 🔗 Related Links
- [Main Platform README](../README.md)