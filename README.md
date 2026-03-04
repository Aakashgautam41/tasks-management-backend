# Tasks Management Backend

A full-featured **Task Management REST API** built with **Spring Boot 3.3.5** and **Java 17**. This application provides a robust backend for managing tasks and subtasks with user authentication, file attachments, email notifications, event-driven messaging, and more.

---

## Features

- **Authentication & Authorization** — JWT-based login/register with role-based access (`ROLE_USER`, `ROLE_ADMIN`)
- **Task CRUD** — Create, read, update, and delete tasks with validation
- **Subtask Management** — Hierarchical subtasks linked to parent tasks
- **Filtering, Sorting & Pagination** — Filter tasks by priority, status, and deadline; sort by any field; paginated responses
- **File Attachments** — Upload file attachments to tasks via **AWS S3**
- **Email Notifications** — Send emails using **AWS SES**
- **Push Notifications** — Publish alerts for critical tasks via **AWS SNS**
- **Event Streaming** — Kafka-based event publishing and consumption for task lifecycle events
- **Caching** — In-memory caching with **Caffeine** for fast reads
- **Scheduled Reminders** — Cron-based task deadline reminder service
- **API Documentation** — Interactive Swagger UI powered by **SpringDoc OpenAPI**
- **Validation** — Bean Validation (Jakarta) with a global exception handler
- **Optimistic Locking** — `@Version`-based concurrency control on tasks
- **CORS Configuration** — Configurable allowed origins for frontend integration

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Framework** | Spring Boot 3.3.5 |
| **Language** | Java 17 |
| **Build Tool** | Maven |
| **Database** | H2 (in-memory) |
| **ORM** | Spring Data JPA / Hibernate |
| **Security** | Spring Security + JWT (jjwt 0.11.5) |
| **Caching** | Spring Cache + Caffeine |
| **Messaging** | Apache Kafka + Spring Kafka |
| **Cloud Services** | AWS SDK v2 (S3, SES, SNS) |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Validation** | Jakarta Bean Validation |
| **Testing** | JUnit 5 + Spring Boot Test + Spring Security Test |
| **Containerization** | Docker Compose (Kafka + Zookeeper) |

---

## Project Structure

```
src/main/java/com/example/tasks_management_backend/
├── TasksManagementBackendApplication.java   # Application entry point
├── config/
│   ├── AwsConfig.java                       # AWS SDK client configuration
│   ├── CacheConfig.java                     # Caffeine cache configuration
│   ├── KafkaTopicConfig.java                # Kafka topic setup
│   ├── LoggingAspect.java                   # AOP-based logging
│   ├── OpenApiConfig.java                   # Swagger/OpenAPI config
│   ├── SecurityConfig.java                  # Spring Security & JWT filter chain
│   ├── TaskDataInitializer.java             # Seed data on startup
│   └── WebConfig.java                       # CORS configuration
├── controller/
│   ├── AuthController.java                  # Login & registration endpoints
│   ├── TaskController.java                  # Task CRUD & attachment endpoints
│   └── SubTaskController.java               # Subtask update & delete endpoints
├── dto/
│   ├── ApiResponse.java                     # Standardized API response wrapper
│   ├── TaskRequest.java                     # Task creation/update DTO
│   └── SubTaskRequest.java                  # Subtask creation/update DTO
├── model/
│   ├── Task.java                            # Task entity (Priority, Status enums)
│   ├── SubTask.java                         # SubTask entity
│   ├── User.java                            # User entity
│   ├── Role.java                            # Role enum (ROLE_USER, ROLE_ADMIN)
│   └── UserRegistrationRequest.java         # Registration request model
├── repository/
│   ├── TaskRepository.java                  # Task JPA repository
│   ├── SubTaskRepository.java               # SubTask JPA repository
│   └── UserRepository.java                  # User JPA repository
├── service/
│   ├── TaskService.java                     # Core task business logic
│   ├── UserService.java                     # User management
│   ├── JwtUtil.java                         # JWT token generation & validation
│   ├── JwtAuthenticationFilter.java         # JWT request filter
│   ├── MyUserDetailsService.java            # UserDetailsService implementation
│   ├── S3Service.java                       # AWS S3 file upload
│   ├── SesEmailService.java                 # AWS SES email sending
│   ├── EmailService.java                    # Email service abstraction
│   ├── SnsService.java                      # AWS SNS notifications
│   ├── KafkaProducerService.java            # Kafka event producer
│   ├── KafkaConsumerService.java            # Kafka event consumer
│   └── TaskReminderService.java             # Scheduled task reminders
└── validations/
    └── ValidationExceptionHandler.java      # Global validation error handler
```

---

## Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **Docker & Docker Compose** (for Kafka)
- **AWS Account** (optional — for S3, SES, SNS features)

### 1. Clone the Repository

```bash
git clone https://github.com/Aakashgautam41/tasks-management-backend.git
cd tasks-management-backend
```

### 2. Start Kafka (Docker Compose)

The project uses Kafka for event streaming. Start the infrastructure services:

```bash
docker-compose up -d
```

This starts:
- **Zookeeper** on port `22181`
- **Kafka** on port `9092`
- **Zoonavigator** (Zookeeper UI) on port `9001`
- **Kafka UI** on port `8090`

### 3. Configure Application Properties

Edit `src/main/resources/application.properties` to update:

```properties
# JWT Secret (change this in production!)
jwt.secret-key=your-secret-key-should-be-kept-safe
jwt.expiration-time=36000000

# AWS Credentials (required for S3/SES/SNS features)
aws.accessKeyId=YOUR_AWS_ACCESS_KEY
aws.secretAccessKey=YOUR_AWS_SECRET_KEY
aws.region=eu-north-1
aws.ses.sourceEmail=your-email@example.com
aws.sns.topicArn=arn:aws:sns:REGION:ACCOUNT_ID:TopicName
aws.s3.bucketName=your-s3-bucket-name

# CORS (set to your frontend URL)
app.cors.allowed-origins=http://localhost:5173
```

### 4. Build & Run

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The API will be available at **`http://localhost:8080`**.

### 5. Access the Tools

| Tool | URL |
|---|---|
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| **H2 Console** | [http://localhost:8080/h2-console](http://localhost:8080/h2-console) |
| **Kafka UI** | [http://localhost:8090](http://localhost:8090) |
| **Zoonavigator** | [http://localhost:9001](http://localhost:9001) |

> **H2 Console** — JDBC URL: `jdbc:h2:mem:todosdb`, Username: `sa`, Password: *(empty)*

---

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/auth/register` | Register a new user | ❌ |
| `POST` | `/auth/login` | Login and receive JWT token | ❌ |

### Tasks

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/tasks` | Get all tasks (with filtering, sorting, pagination) | ✅ |
| `GET` | `/api/tasks/{id}` | Get a specific task | ✅ |
| `POST` | `/api/tasks` | Create a new task | ✅ |
| `PUT` | `/api/tasks/{id}` | Update an existing task | ✅ |
| `DELETE` | `/api/tasks/{id}` | Delete a task | ✅ |
| `POST` | `/api/tasks/{id}/attachment` | Upload a file attachment | ✅ |

### Subtasks

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/tasks/{taskId}/subtasks` | Create a subtask for a task | ✅ |
| `GET` | `/api/tasks/{taskId}/subtasks` | Get all subtasks for a task | ✅ |
| `PUT` | `/api/subtasks/{id}` | Update a subtask | ✅ |
| `DELETE` | `/api/subtasks/{id}` | Delete a subtask | ✅ |

### Query Parameters for `GET /api/tasks`

| Parameter | Type | Description | Default |
|---|---|---|---|
| `priority` | `string` | Filter by priority (`LOW`, `MEDIUM`, `HIGH`) | — |
| `status` | `string` | Filter by status (`PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`) | — |
| `deadlineBefore` | `date` | Filter tasks with deadline before this date (ISO format) | — |
| `sortBy` | `string` | Field to sort by | — |
| `direction` | `string` | Sort direction (`asc` or `desc`) | `asc` |
| `page` | `int` | Page number (zero-based) | `0` |
| `size` | `int` | Page size | `10` |

---

## API Response Format

All responses follow a consistent wrapper format:

```json
{
  "success": true,
  "status": 200,
  "message": "Tasks retrieved successfully",
  "data": { ... }
}
```

---

## Authentication Flow

1. **Register** a new user:
   ```bash
   curl -X POST http://localhost:8080/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username": "john", "password": "secret123", "email": "john@example.com"}'
   ```

2. **Login** to receive a JWT token:
   ```bash
   curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "john", "password": "secret123"}'
   ```

3. **Use the token** in subsequent requests:
   ```bash
   curl -X GET http://localhost:8080/api/tasks \
     -H "Authorization: Bearer <your-jwt-token>"
   ```

---

## Data Models

### Task

| Field | Type | Constraints |
|---|---|---|
| `id` | `Long` | Auto-generated |
| `title` | `String` | Required, 3–255 characters |
| `priority` | `Enum` | `LOW`, `MEDIUM`, `HIGH` (required) |
| `status` | `Enum` | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` (required) |
| `deadline` | `LocalDate` | Must be today or in the future |
| `attachmentUrl` | `String` | S3 URL of uploaded file |
| `subtasks` | `List<SubTask>` | Child subtasks |
| `version` | `Integer` | Optimistic lock version |

### User

| Field | Type | Constraints |
|---|---|---|
| `id` | `Long` | Auto-generated |
| `username` | `String` | Required, unique |
| `password` | `String` | Required (stored hashed) |
| `email` | `String` | Valid email format |
| `roles` | `Set<Role>` | `ROLE_USER`, `ROLE_ADMIN` |

---

## Running Tests

```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=TaskServiceTest

# Run integration tests
./mvnw test -Dtest=TaskControllerIntegrationTest
```

The project includes:
- **Unit Tests** — `TaskServiceTest`
- **Integration Tests** — `AuthControllerIntegrationTest`, `TaskControllerIntegrationTest`

---

## Docker Services

The `docker-compose.yml` provides the following services:

| Service | Image | Port | Description |
|---|---|---|---|
| Zookeeper | `confluentinc/cp-zookeeper:7.6.0` | `22181` | Kafka coordination |
| Kafka | `confluentinc/cp-kafka:7.6.0` | `9092` | Message broker |
| Zoonavigator | `elkozmon/zoonavigator:latest` | `9001` | Zookeeper web UI |
| Kafka UI | `provectuslabs/kafka-ui:latest` | `8090` | Kafka web UI |

---

## Configuration Reference

| Property | Description | Default |
|---|---|---|
| `spring.datasource.url` | Database JDBC URL | `jdbc:h2:mem:todosdb` |
| `jwt.secret-key` | JWT signing secret | — |
| `jwt.expiration-time` | JWT token expiry (ms) | `36000000` (10 hours) |
| `app.cors.allowed-origins` | Allowed CORS origins | `http://localhost:5173` |
| `app.task.cron` | Task reminder cron expression | `0 */2 * * * ?` (every 2 min) |
| `spring.kafka.bootstrap-servers` | Kafka broker address | `localhost:9092` |
| `aws.region` | AWS region | `eu-north-1` |
| `aws.s3.bucketName` | S3 bucket for attachments | — |
| `aws.ses.sourceEmail` | Verified SES sender email | — |
| `aws.sns.topicArn` | SNS topic ARN for alerts | — |
