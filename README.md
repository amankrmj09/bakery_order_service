# Bakery Order Service

This is the order management microservice for Blu's Bakery.

## Folder Structure

```text
src
+---main
|   +---java
|   |   \---com
|   |       \---blubugtech
|   |           \---bakery_order_service
|   |               +---client       // Feign clients to other services
|   |               +---constants    // Constants
|   |               +---controller   // REST API endpoints
|   |               +---dto          // Data Transfer Objects for API request/response
|   |               +---entity       // JPA Entities
|   |               +---enums        // Enumerations (e.g., OrderStatus, DeliveryType)
|   |               +---event        // Internal domain events
|   |               +---exception    // Custom exceptions and GlobalExceptionHandler
|   |               +---gateway      // Gateways for external communications
|   |               +---integration  // Kafka producers/consumers
|   |               +---inventory    // Inventory checking logic
|   |               +---mapper       // MapStruct mappers
|   |               +---pricing      // Pricing calculation strategies
|   |               +---repository   // Spring Data JPA repositories
|   |               +---service      // Business logic services
|   |               \---validation   // Custom validators
|   \---resources
|       +---db/migration             // Flyway database migration scripts
|       \---application.yml          // Configuration files
\---test                             // Unit and integration tests
```

## API Documentation

For complete API documentation with endpoints and examples, please refer to [API_REFERENCE.md](./API_REFERENCE.md).

## 🔗 Related Links

*For overall architecture, contribution guidelines, and security policies, please refer to the main [Blu's Bakery](https://github.com/amankrmj09/Blu_s_Bakery) repository.*

