# Order Service

This microservice is part of the IRP Modernization project and is responsible for handling product orders. It communicates with the Inventory Service to check product availability and update inventory after orders are placed.

## Architecture

The Order Service follows a layered architecture:

- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic
- **Repository Layer**: Manages data access
- **Model Layer**: Represents database entities
- **DTO Layer**: Data Transfer Objects for API communication

## Features

- Create new orders
- Retrieve orders by ID
- Retrieve all orders
- Communicate with Inventory Service to check availability
- Update inventory after order placement

## API Endpoints

### POST /order
Places a new order and updates inventory accordingly.

**Request Body:**
```json
{
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "orderItems": [
    {
      "productId": 1,
      "quantity": 5,
      "price": 10.0,
      "handlerType": "FIFO"
    }
  ]
}
```

**Response:**
```json
{
  "id": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "orderDate": "2025-11-27T18:30:00",
  "status": "PLACED",
  "orderItems": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 5,
      "price": 10.0,
      "handlerType": "FIFO"
    }
  ],
  "totalAmount": 50.0
}
```

### GET /order/{id}
Retrieves an order by its ID.

### GET /order
Retrieves all orders in the system.

### GET /order/customer/{email}
Retrieves all orders for a specific customer by email.

## Communication with Inventory Service

The Order Service communicates with the Inventory Service using RestTemplate for the following operations:

1. **Check Inventory**: `GET /inventory/{productId}`
   - Retrieves available inventory for a product before placing an order

2. **Update Inventory**: `POST /inventory/update`
   - Updates inventory after an order is placed
   - Payload includes:
     - `productId`: ID of the product
     - `quantity`: Quantity to be deducted
     - `handlerType`: Inventory handling strategy (e.g., FIFO, LIFO)

## Technical Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database (in-memory)
- JUnit 5 and Mockito for testing
- Swagger/OpenAPI for API documentation

## Running the Application

1. Start the Inventory Service first
2. Start the Order Service:
   ```
   ./mvnw spring-boot:run
   ```
3. The service will run on port 8081
4. Access the H2 console at: http://localhost:8081/h2-console
   - JDBC URL: `jdbc:h2:mem:orderdb`
   - Username: `sa`
   - Password: `password`
   - No additional settings needed, just click "Connect"
5. Access the API documentation at: http://localhost:8081/swagger-ui.html

## Testing

The application includes both unit tests and integration tests:

- Unit tests for service layer logic
- Integration tests for REST endpoints using @SpringBootTest
