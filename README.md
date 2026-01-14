# Cymelle Technologies - Full Stack Developer Test

This is a Spring Boot backend service for Cymelle Technologies platform, managing products, users, orders, and rides.

## Features

- **Product Management**: CRUD operations for products (Admin only for modifications).
- **User Authentication**: Register and Login using JWT. Roles: `ADMIN`, `CUSTOMER`, `DRIVER`.
- **Ecommerce Orders**: Place orders, view details, update status. Search capability.
- **Hailing App Rides**: Request rides, view details, update status. Search capability.
- **Search**: Search products by name/category, orders/rides by status.
- **Bonus**: 
    - Swagger UI for API documentation.
    - Global Error Handling & Standardized Responses.
    - Simulated Payment Gateway.
    - PostgreSQL Database.

## Tech Stack

- Java 21
- Spring Boot 3.x
- Spring Security & JWT
- Spring Data JPA
- PostgreSQL
- Lombok
- Swagger / OpenAPI

## Setup Instructions

### Prerequisites
- JDK 21
- Maven
- PostgreSQL Database

### Database Setup
1. Install PostgreSQL.
2. Create a database named `cymelle_db`.
3. Update `src/main/resources/application.properties` with your database credentials (defaults provided below):
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/cymelle_db
   spring.datasource.username=postgres
   spring.datasource.password=password
   ```

### Running the Application
1. Clone the repository.
2. Navigate to the project root.
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
4. The application will start on port 8080.

### Running with Docker (Recommended)
*Best for: Quick start, no extra dependencies required.*

You can also run the application fully containerized with PostgreSQL using Docker Compose.

1.  **Prerequisites**: Ensure **Docker Desktop** is installed and running.
2.  **Clone the application from GitHub**:
    ```powershell
    git clone https://github.com/chess254/cymelle.git
    cd cymelle
    ./mvnw clean package
    ```
2.  **Run the Application**:
    ```powershell
    docker-compose up --build
    ```
3.  **Result**:
    *   The backend will start at `http://localhost:8080`.
    *   The PostgreSQL database will start automatically on port `5432`.
    *   You are ready to go!

### API Documentation
Once the application is running, you can access the Swagger UI at:
http://localhost:8080/swagger-ui.html or alternatively use the postman collection Cymelle_API_Collection.json thats in the repo.

## Testing
- **Integration Tests**: Run `./mvnw test` to execute the full integration test suite.
- **Postman (recommended)**: A sample Postman collection with dummy data is provided: `Cymelle_API_Collection.json`. Import this into Postman to test the endpoints.
- **Swagger UI**: Access comprehensive documentation and test endpoints at `http://localhost:8080/swagger-ui.html` when the app is running.
- **Authentication**: First, register a user via `/api/v1/auth/register`. Then authenticate via `/api/v1/auth/authenticate` to get a JWT token. Use this token in the `Authorization` header as `Bearer <token>` for secured endpoints.
- **Setup Test Users**: To test the different system capabilities, register the following roles:
  - **Admin**: Use `role: "ADMIN"` in the registration payload. Required for managing products and updating order statuses.
  - **Driver**: Use `role: "DRIVER"` in the registration payload. Required for accepting and completing rides.
  - **Customer**: Use `role: "CUSTOMER"` (default). Used for placing orders and requesting rides.

### Sample Registration Payloads
For testing purposes, you can use these payloads at `POST /api/v1/auth/register`:
- **Admin**: `{"firstName": "Admin", "lastName": "User", "email": "admin@cymelle.com", "password": "password123", "role": "ADMIN"}`
- **Customer**: `{"firstName": "John", "lastName": "Doe", "email": "customer@example.com", "password": "password123", "role": "CUSTOMER"}`
- **Driver**: `{"firstName": "Fast", "lastName": "Driver", "email": "driver@example.com", "password": "password123", "role": "DRIVER"}`

## System Enums & Logic

The application uses specific status flows and roles to manage business logic.

### 1. User Roles (`Role`)
- **`ADMIN`**: System administrators. Can manage (CRUD) products and update the status of any order or ride.
- **`CUSTOMER`**: Default users. Can browse products, place orders, and request rides. They can only see their own history.
- **`DRIVER`**: Specialized service providers. They can browse `REQUESTED` rides and accept them to become the assigned driver.

### 2. Order Lifecycle (`OrderStatus`)
- **`PENDING`**: Initial state when an order is first placed by a customer.
- **`SHIPPED`**: Set by an Admin when the order has left the warehouse.
- **`DELIVERED`**: Final state when the customer has received their items.
- **`CANCELLED`**: The order was voided and will not be fulfilled.

### 3. Ride Lifecycle (`RideStatus`)
- **`REQUESTED`**: Initial state when a customer requests a ride. It is visible to all drivers.
- **`ACCEPTED`**: A Driver has accepted the ride. At this point, the driver is assigned and a dummy fare is calculated.
- **`COMPLETED`**: The ride has reached its destination. The completion time is recorded.
- **`CANCELLED`**: The ride was cancelled by the customer or system before completion. Note: Completed rides cannot be cancelled.

## CI/CD Pipeline
The project includes a GitHub Actions workflow defined in `.github/workflows/maven.yml` that automates testing and deployment steps:

1.  **Trigger**: Runs on every push or pull request to the `main` or `master` branches.
2.  **Build & Test**: Sets up JDK 21 and runs `./mvnw clean install` to compile the code and execute all unit and integration tests.
3.  **Deployment (Docker)**: Builds a Docker image (`cymelle-backend:latest`) from the tested artifact, ensuring the application is ready for containerized deployment.

## Project Structure
- `controller`: REST API endpoints handling incoming HTTP requests.
- `service`: Business logic layer.
- `repository`: Data access interfaces (Spring Data JPA).
- `model`: JPA Entities (User, Product, Order, Ride, etc.) and Enums.
- `dto`: Data Transfer Objects for request and response mapping.
- `config`: Security configuration, CORS, and application beans.
- `security`: JWT Filter, Token Service, and specialized error handlers.
- `exception`: Global Exception Handling logic and custom Exception classes.
