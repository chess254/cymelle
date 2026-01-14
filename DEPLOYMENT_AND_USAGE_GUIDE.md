# Deployment and Usage Guide

This guide provides step-by-step instructions for deploying the project to GitHub and for other developers to clone and run the application.

## Part 1: For the Project Owner (Pushing to GitHub)

Follow these steps to upload your local project to a GitHub repository.

1.  **Initialize Git** (if not already done):
    ```powershell
    git init
    ```

2.  **Add Files**:
    Stage all your project files for the initial commit.
    ```powershell
    git add .
    ```

3.  **Commit Changes**:
    Create the first commit.
    ```powershell
    git commit -m "Initial commit: Complete Spring Boot backend with Docker and CI/CD"
    ```

4.  **Create a Repository on GitHub**:
    *   Go to [github.com/new](https://github.com/new).
    *   Name it `cymelle-backend` (or a name of your choice).
    *   **Important**: Do not add a README, .gitignore, or license file during creation (these already exist in your local project).
    *   Click **Create repository**.

5.  **Connect and Push**:
    Copy the commands provided by GitHub under "…or push an existing repository from the command line" and run them in your terminal. They will look similar to this:
    ```powershell
    git branch -M main
    git remote add origin https://github.com/YOUR_USERNAME/cymelle-backend.git
    git push -u origin main
    ```

---

## Part 2: For Collaborators (Cloning & Running)

These instructions are for a new user who wants to download and run the application on their machine.

### Step 1: Clone the Repository
Open a terminal and run the following commands to download the code:
```powershell
git clone https://github.com/YOUR_USERNAME/cymelle-backend.git
cd cymelle-backend
```

### Option A: Running with Docker (Recommended)
*Best for: Quick start, no extra dependencies required.*

1.  **Prerequisites**: Ensure **Docker Desktop** is installed and running.
2.  **Run the Application**:
    ```powershell
    docker-compose up --build
    ```
3.  **Result**:
    *   The backend will start at `http://localhost:8080`.
    *   The PostgreSQL database will start automatically on port `5432`.
    *   You are ready to go!

### Option B: Running Manually (Without Docker)
*Best for: Development, debugging, or if Docker is not available.*

1.  **Prerequisites**:
    *   **Java 21 SDK** installed.
    *   **PostgreSQL** installed and running as a service.

2.  **Database Setup**:
    *   Create a new database in PostgreSQL named `cymelle_db`:
        ```sql
        CREATE DATABASE cymelle_db;
        ```
    *   Open `src/main/resources/application.properties` and update the database credentials to match your local PostgreSQL setup:
        ```properties
        spring.datasource.username=postgres
        spring.datasource.password=your_actual_password
        ```

3.  **Run the Application**:
    *   Use the Maven wrapper included in the repository:
        ```powershell
        ./mvnw spring-boot:run
        ```
    *   The app will start at `http://localhost:8080`.

---

## Verifying the Application
Once the application is running (via Docker or Manually), verify it is working:

1.  **Access API Documentation**:
    Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) in your browser.

2.  **Test Registration Endpoint**:
    You can use Swagger UI or Postman to send a `POST` request to `/api/v1/auth/register` with the following JSON body:
    ```json
    {
      "firstName": "Jane",
      "lastName": "Doe",
      "email": "jane@example.com",
      "password": "password123",
      "role": "ADMIN"
    }
    ```
