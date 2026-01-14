# Cymelle Technologies - Full Stack Developer Test

This is a Spring Boot backend service for Cymelle Technologies platform, managing products, users, orders, and rides.

## Features

- **Product Management**: CRUD operations for products (Admin only for modifications).
- **User Authentication**: Register and Login using JWT. Roles: ADMIN, CUSTOMER.
- **Ecommerce Orders**: Place orders, view details, update status. Search capability.
- **Hailing App Rides**: Request rides, view details, update status. Search capability.
- **Search**: Search products by name/category, orders/rides by status.
- **Bonus**: 
    - Swagger UI for API documentation.
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
3. Update `src/main/resources/application.properties` with your database credentials:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### Running the Application
1. Clone the repository.
2. Navigate to the project root.
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
4. The application will start on port 8080.

### Running with Docker

You can also run the application fully containerized with PostgreSQL using Docker Compose.

1.  Make sure you have **Docker** and **Docker Compose** installed.
2.  Build and run the containers:
    ```bash
    docker-compose up --build
    ```
    *This command will bundle the application and start both the Backend (on port 8080) and the PostgreSQL database (on port 5432).*
3.  To stop the application:
    ```bash
    docker-compose down
    ```

### API Documentation
Once the application is running, you can access the Swagger UI at:
http://localhost:8080/swagger-ui.html

## Testing
- Use Postman or Swagger UI to test the endpoints.
- **Authentication**: First, register a user via `/api/v1/auth/register`. Then authenticate via `/api/v1/auth/authenticate` to get a JWT token. Use this token in the `Authorization` header as `Bearer <token>` for secured endpoints.
- **Admin Access**: To access Admin endpoints (Product modifications), you might need to manually set the role to `ADMIN` in the database or register with `role: "ADMIN"` (if the DTO allows/you modify the logic to allow role selection). *Note: The current registration endpoint allows passing role.*

## CI/CD Pipeline
The project includes a GitHub Actions workflow defined in `.github/workflows/maven.yml` that automates testing and deployment steps:

1.  **Trigger**: Runs on every push or pull request to the `main` or `master` branches.
2.  **Build & Test**: Sets up JDK 21 and runs `./mvnw clean install` to compile the code and execute all unit and integration tests.
3.  **Deployment (Docker)**: Builds a Docker image (`cymelle-backend:latest`) from the tested artifact, ensuring the application is ready for containerized deployment.

## Project Structure
- `model`: JPA Entities (User, Product, Order, Ride).
- `repository`: Data access interfaces.
- `service`: Business logic.
- `controller`: REST API endpoints.
- `config`: Security and App configuration.
- `dto`: Data Transfer Objects.
- `security`: JWT filter and service.
