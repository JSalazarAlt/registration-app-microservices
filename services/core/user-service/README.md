# User Service

User profile microservice for managing user information and preferences.

## Port

- **8082** - User Service

## Tech Stack

- Spring Boot 3.5.9
- Spring Data JPA
- Spring Security (OAuth2 Resource Server)
- Spring Kafka
- MySQL
- MapStruct
- Lombok

## Features

- User profile management (CRUD)
- User search by name
- Paginated user listing
- JWT authentication (OAuth2 Resource Server)
- Kafka event consumption (user-creation, account-email-update, account-username-update)
- Async event processing with @Async
- Swagger API documentation

## Database

- **Database:** suyos_user_db
- **Port:** 3307
- **Tables:** users

## API Endpoints

### User Operations
- `GET /api/v1/users/{userId}` - Get user by ID
- `PUT /api/v1/users/{userId}` - Update user by ID
- `GET /api/v1/users/account/{accountId}` - Get user by account ID
- `PUT /api/v1/users/account/{accountId}` - Update user by account ID
- `GET /api/v1/users` - Get all users (paginated)
- `GET /api/v1/users/search` - Search users by name
- `POST /internal/users` - Create user (internal endpoint)

## Kafka Topics

### Consumed Events
- `user-creation` - Creates user profile when account is registered
- `account-email-update` - Updates user email when changed in Auth Service
- `account-username-update` - Updates username when changed in Auth Service

## Configuration

```properties
# JWT
jwt.public-key=classpath:keys/public_key.pem

# Database
spring.datasource.url=jdbc:mysql://localhost:3307/suyos_user_db

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=user-service-group
```

## Error Documentation

- `/docs/errors/user-not-found.md` - User not found error

## Swagger UI

http://localhost:8082/swagger-ui.html

## Run

```bash
mvn spring-boot:run
```
