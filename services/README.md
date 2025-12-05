# Services

This directory contains the backend microservices for the Registration App.

## Architecture

The backend follows a microservices architecture with the following services:

### 🔐 Auth Service (Port 8080)
- **Purpose**: Authentication and account management
- **Technology**: Spring Boot, Spring Security, Spring OAuth2 Resource Server, Spring Kafka
- **Database**: MySQL - Handles account data, authentication tokens
- **Key Features**:
  - User registration and login
  - JWT token generation and validation (RSA asymmetric keys)
  - OAuth2 Google integration with account linking
  - Account security (locking after 5 failed attempts, 24h lockout)
  - Password encryption with BCrypt
  - Refresh token rotation
  - Account soft deletion
  - Kafka event producer (async, non-blocking)
  - Structured logging to ELK stack
  - Prometheus metrics
  - Error documentation at `/docs/errors/`

### 👤 User Service (Port 8081)
- **Purpose**: User profile management
- **Technology**: Spring Boot, Spring Data JPA, Spring OAuth2 Resource Server, Spring Kafka
- **Database**: MySQL - Handles user profile data
- **Key Features**:
  - User profile CRUD operations
  - JWT validation (shared RSA public key)
  - Profile search and pagination
  - Terms and privacy policy tracking
  - Profile picture management
  - Email/username sync from Auth Service
  - Kafka event consumer (async with @Async, error handling, 3 retries, 2s backoff)
  - Structured logging to ELK stack
  - Prometheus metrics
  - Error documentation at `/docs/errors/`

### 🌐 BFF Service (Port 3001)
- **Purpose**: Backend-for-Frontend data aggregation
- **Technology**: NestJS, TypeScript, Axios
- **Key Features**:
  - Aggregates data from Auth and User services
  - Simplified API for frontend consumption
  - Request routing and transformation
  - Cross-service data composition
  - Complete profile endpoint (account + user data)

## Shared Libraries

### 📦 Common Library
- **Location**: `shared/common`
- **Purpose**: Shared DTOs, Events, and utilities across microservices
- **Contents**:
  - `PagedResponseDTO` - Generic pagination wrapper
  - `UserCreationEvent` - User creation event
  - `AccountUsernameUpdateEvent` - Username update event
  - `AccountEmailUpdateEvent` - Email update event
  - `ApiException` - Base exception class
  - `ErrorCode` - Error code enum
- **Usage**: Imported as Maven dependency in Auth and User services

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+ (for BFF service)
- pnpm (for BFF service)
- Docker (optional, for Kafka, ELK, Monitoring)
- Kafka (optional, for event-driven features)

### Infrastructure Setup (Optional)
```bash
# Start Kafka
cd ../infrastructure
docker-compose up -d

# Start ELK Stack
cd ../elk
docker-compose -f docker-compose-elk.yml up -d

# Start Monitoring
cd ../monitoring
docker-compose -f docker-compose-monitoring.yml up -d
```

### Setup Shared Library
```bash
cd shared/common
mvn clean install
```

### Running Services

1. **Start Auth Service**:
   ```bash
   cd authservice
   mvn spring-boot:run
   ```

2. **Start User Service**:
   ```bash
   cd userservice
   mvn spring-boot:run
   ```

3. **Start BFF Service**:
   ```bash
   cd bffservice
   pnpm install
   pnpm run start:dev
   ```

## Service Communication

```
Frontend → BFF Service → Auth Service (8080)
                      → User Service (8081)

Auth Service → Kafka Topics → User Service (async)
  - user-creation
  - account-username-update
  - account-email-update

All Services → Logstash:5000 → Elasticsearch → Kibana:5601
All Services → Prometheus:9090 → Grafana:3000
```

- Frontend communicates only with BFF Service
- BFF Service aggregates data from microservices
- Auth Service publishes events to Kafka (async, non-blocking)
- User Service consumes events from Kafka (async with @Async, error handling, retry logic)
- Services validate JWT tokens independently using shared RSA public key
- Services communicate via HTTP REST APIs and Kafka events
- Structured logs sent to ELK stack for centralized logging
- Metrics exposed to Prometheus for monitoring

## Security

### JWT Authentication
- **Algorithm**: RS256 (RSA asymmetric encryption)
- **Key Management**: Private key in Auth Service, public key shared with User Service
- **Token Types**: Access token (15min) + Refresh token (7 days)
- **Validation**: Each service validates tokens independently using OAuth2 Resource Server

### Endpoints Security
- **Public**: `/internal/**`, `/actuator/**`, `/swagger-ui/**`, `/docs/**`
- **Protected**: All other endpoints require valid JWT token
- **CORS**: Configured for `http://localhost:5173` and `http://localhost:3001`

## Error Documentation

Comprehensive error documentation available:
- **Auth Service**: `authservice/docs/errors/` (17 error types)
- **User Service**: `userservice/docs/errors/` (1 error type)

Each error document includes:
- HTTP status code
- Error code
- Description
- Common causes
- Resolution steps
- Related endpoints

## Development

### Building All Services
```bash
# Shared Library (required first)
cd shared/common && mvn clean install

# Auth Service
cd services/authservice && mvn clean install

# User Service  
cd services/userservice && mvn clean install

# BFF Service
cd services/bffservice && pnpm install && pnpm run build
```

### Testing
```bash
# Run tests for Spring Boot services
cd authservice && mvn test
cd userservice && mvn test

# Run tests for BFF service
cd bffservice && pnpm test
```

### Docker Build
```bash
# See ../BUILD.md for detailed commands
cd authservice && docker build -t authservice:latest .
cd userservice && docker build -t userservice:latest .
cd bffservice && docker build -t bffservice:latest .
```

## API Documentation

- **Auth Service Swagger**: http://localhost:8080/swagger-ui.html
- **User Service Swagger**: http://localhost:8081/swagger-ui.html
- **Auth Service Error Docs**: `authservice/docs/errors/`
- **User Service Error Docs**: `userservice/docs/errors/`

## Monitoring & Logging

- **Kibana (Logs)**: http://localhost:5601
- **Prometheus (Metrics)**: http://localhost:9090
- **Grafana (Dashboards)**: http://localhost:3000

## Event-Driven Architecture

### Kafka Topics
- `user-creation` - Published when new user registers
- `account-username-update` - Published when username changes
- `account-email-update` - Published when email changes

### Producer (Auth Service)
- Uses `KafkaTemplate.send()` - async by default (non-blocking)
- Publishes events after account operations

### Consumer (User Service)
- Uses `@KafkaListener` with `@Async` annotation
- Async processing with error handling
- Retry logic: 3 retries with 2 second backoff
- Non-blocking database operations