# Core Microservices

This directory contains all core backend microservices built with Spring Boot.

## Services

### Auth Service (Port 8081)
**Purpose:** Authentication and account management

**Responsibilities:**
- User registration and login
- JWT token generation (RS256 with private key)
- OAuth2 integration (Google)
- Account security (locking after 5 failed attempts, 24h lockout)
- Password encryption with BCrypt
- Refresh token rotation
- Account soft deletion
- Kafka event producer (user-creation, account-username-update, account-email-update)

**Tech Stack:**
- Spring Boot 3.5.8
- Spring Security + OAuth2 Client + OAuth2 Resource Server
- Spring Data JPA + MySQL
- Spring Kafka
- JWT (JJWT library)
- MapStruct for DTO mapping
- Logback with Logstash encoder
- Prometheus metrics
- OpenAPI/Swagger documentation

**Database:** suyos_auth_db (MySQL port 3306)

**Key Endpoints:**
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/logout` - User logout
- `POST /api/v1/auth/refresh` - Refresh JWT token
- `GET /api/v1/accounts/{username}` - Get account by username
- `GET /api/v1/accounts/me` - Get current logged-in account
- `PATCH /api/v1/accounts/me` - Update current logged-in account

**Swagger:** http://localhost:8081/swagger-ui.html

---

### User Service (Port 8082)
**Purpose:** User profile management

**Responsibilities:**
- User profile CRUD operations
- Personal information storage
- Profile picture and preferences
- User search and pagination
- Terms and privacy policy acceptance tracking
- Email/username sync from Auth Service via Kafka
- User soft deletion
- Kafka event consumer (async processing with @Async, error handling, 3 retries)

**Tech Stack:**
- Spring Boot 3.5.8
- Spring Security + OAuth2 Resource Server
- Spring Data JPA + MySQL
- Spring Kafka
- MapStruct for DTO mapping
- Logback with Logstash encoder
- Prometheus metrics
- OpenAPI/Swagger documentation

**Database:** suyos_user_db (MySQL port 3307)

**Key Endpoints:**
- `GET /api/v1/users/{userId}` - Get user profile by ID
- `PUT /api/v1/users/{userId}` - Update user profile by ID
- `GET /api/v1/users/account/{accountId}` - Get user profile by account ID
- `PUT /api/v1/users/account/{accountId}` - Update user profile by account ID
- `GET /api/v1/users` - Get all users (paginated)
- `GET /api/v1/users/search` - Search users by name
- `POST /internal/users` - Create user (internal endpoint)

**Swagger:** http://localhost:8082/swagger-ui.html

---

### API Gateway (Port 8080)
**Purpose:** Single entry point for all external traffic

**Responsibilities:**
- Request routing to microservices
- JWT token validation (OAuth2 Resource Server)
- Rate limiting (10 req/sec, burst 20)
- Circuit breaker (Resilience4j)
- CORS configuration
- Load balancing via Eureka service discovery
- Distributed tracing with trace_id propagation
- Global logging filter

**Tech Stack:**
- Spring Boot 3.3.13
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka Client
- Spring Cloud LoadBalancer
- Resilience4j Circuit Breaker
- Spring Security + OAuth2 Resource Server
- Redis (for rate limiting)
- Logback with Logstash encoder
- Prometheus metrics

**Routes:**
- `/api/v1/auth/**` → Auth Service (8081)
- `/api/v1/accounts/**` → Auth Service (8081)
- `/api/v1/users/**` → User Service (8082)
- `/actuator/**` → Gateway actuator endpoints

**Public Endpoints:** `/api/v1/auth/**`, `/actuator/**`

---

### Email Service (Port 8083)
**Purpose:** Email notification handling

**Status:** Under development

---

### Session Service (Port 8084)
**Purpose:** Session management

**Status:** Under development

---

## Shared Infrastructure

### Eureka Server (Port 8761)
**Purpose:** Service discovery and registration

All microservices register with Eureka for dynamic service discovery. Gateway uses Eureka to route requests to available service instances.

**Dashboard:** http://localhost:8761

---

### Shared Library (common:1.0.0)
**Location:** `/shared/common`

**Contents:**
- `PagedResponseDTO` - Pagination wrapper
- Event DTOs (`UserCreationEvent`, `AccountUsernameUpdateEvent`, `AccountEmailUpdateEvent`)
- `ApiException` base class
- `ErrorCode` enum

**Usage:** Maven dependency in all microservices

---

## Running Services

### Prerequisites
- Java 21
- Maven 3.6+
- MySQL (ports 3306, 3307)
- Kafka (port 9092)

### Build Shared Library First
```bash
cd shared/common
mvn clean install
```

### Start Services
```bash
# Eureka Server
cd services/eureka-server
mvn spring-boot:run

# Auth Service
cd services/core/auth-service
mvn spring-boot:run

# User Service
cd services/core/user-service
mvn spring-boot:run

# API Gateway
cd services/core/api-gateway
mvn spring-boot:run
```

### Using Spring Boot Dashboard (VS Code)
1. Open Spring Boot Dashboard panel
2. Click play button next to each service
3. Services will start in order: Eureka → Auth → User → Gateway

---

## Architecture Patterns

### Event-Driven Communication
- Auth Service publishes events to Kafka
- User Service consumes events asynchronously
- Topics: `user-creation`, `account-username-update`, `account-email-update`

### JWT Authentication
- Auth Service generates JWT with RS256 (private key)
- Gateway and User Service validate JWT (shared public key)
- Independent token validation at each layer (defense in depth)

### Observability
- Structured JSON logs sent to ELK Stack (Logstash port 5000)
- Prometheus metrics exposed at `/actuator/prometheus`
- Distributed tracing with OpenTelemetry (trace_id propagation)

### Resilience
- Circuit breaker in Gateway (50% failure threshold, 10s open state)
- Rate limiting in Gateway (10 req/sec per user)
- Async processing with retry logic (3 retries, 2s backoff)
- Health checks at `/actuator/health`

---

## Testing

Each service includes:
- Unit tests (JUnit 5 + Mockito)
- Integration tests (TestContainers for MySQL and Kafka)
- Test coverage reporting (JaCoCo - 80% minimum)

Run tests:
```bash
mvn test
```

Generate coverage report:
```bash
mvn jacoco:report
```

---

## API Documentation

- **Auth Service:** http://localhost:8081/swagger-ui.html
- **User Service:** http://localhost:8082/swagger-ui.html
- **Error Documentation:** `/docs/errors/` in each service

---

## Monitoring

- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3000
- **Kibana:** http://localhost:5601
- **Eureka Dashboard:** http://localhost:8761
