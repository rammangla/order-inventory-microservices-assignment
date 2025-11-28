# Inventory Service

A Spring Boot microservice for managing inventory of materials/products. This service is part of a microservices architecture and communicates with the Order Service.

## Features

- Maintain inventory of materials/products
- Track product batches with different expiry dates
- Get inventory batches sorted by expiry date
- Update inventory after an order is placed
- Factory Pattern implementation for extensible inventory handling logic

## Tech Stack

- Java 17
- Spring Boot 4.0.0
- Spring Data JPA
- H2 Database (in-memory)
- Lombok
- OpenAPI/Swagger for API documentation

## API Endpoints

### Get Inventory Batches by Product ID

```
GET /inventory/{productId}
```

Returns a list of inventory batches for the specified product, sorted by expiry date.

### Update Inventory

```
POST /inventory/update
```

Updates inventory after an order is placed.

Request Body:
```json
{
  "productId": 1,
  "quantity": 10,
  "handlerType": "STANDARD"
}
```

## Architecture

### Factory Pattern Implementation

The service implements the Factory Pattern to allow for future extension of inventory handling logic:

- `InventoryHandler` interface defines the contract for inventory handling strategies
- `StandardInventoryHandler` provides the default implementation
- `InventoryHandlerFactory` returns the appropriate handler based on the requested type

This design allows for easy extension of inventory handling logic by adding new implementations of the `InventoryHandler` interface.

### Layered Architecture

The service follows a standard layered architecture:

- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic
- **Repository Layer**: Handles data access
- **Model Layer**: Defines the domain model
- **DTO Layer**: Data Transfer Objects for API communication

## Testing

The service includes comprehensive testing:

### Unit Tests

- Service layer tests using JUnit 5 and Mockito
- Factory pattern implementation tests
- Controller tests using MockMvc

### Integration Tests

- End-to-end tests using `@SpringBootTest`
- Tests with H2 in-memory database
- API endpoint tests using TestRestTemplate

## API Documentation

OpenAPI/Swagger documentation is available at:

```
http://localhost:8082/swagger-ui.html
```

## H2 Database Console

The H2 Database Console is available at:

```
http://localhost:8082/h2-console
```

Connection details:
- JDBC URL: `jdbc:h2:mem:inventorydb`
- Username: `sa`
- Password: `password`

## Sample Data

The service initializes with sample data for testing purposes:
- 3 products (Paracetamol, Amoxicillin, Vitamin C)
- 6 inventory batches with different expiry dates

## Communication with Order Service

The Order Service can call the `POST /inventory/update` endpoint to update inventory after an order is placed.
