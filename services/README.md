# Services

This directory contains the backend microservices for the Registration App.

## Architecture

The backend follows a microservices architecture with the following services:

### 🔐 Auth Service (Port 8080)
- **Purpose**: Authentication and account management
- **Technology**: Spring Boot, Spring Security, Spring OAuth2 Resource Server
- **Database**: MySQL - Handles account data, authentication tokens
- **Key Features**:
  - User registration and login
  - JWT token generation and validation (RSA asymmetric keys)
  - OAuth2 Google integration with account linking
  - Account security (locking after 5 failed attempts, 24h lockout)
  - Password encryption with BCrypt
  - Refresh token rotation
  - Account soft deletion

### 👤 User Service (Port 8081)
- **Purpose**: User profile management
- **Technology**: Spring Boot, Spring Data JPA, Spring OAuth2 Resource Server
- **Database**: MySQL - Handles user profile data
- **Key Features**:
  - User profile CRUD operations
  - JWT validation (shared RSA public key)
  - Profile search and pagination
  - Terms and privacy policy tracking
  - Profile picture management
  - Email/username sync from Auth Service

### 🌐 BFF Service (Port 3000)
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
- **Purpose**: Shared DTOs and utilities across microservices
- **Contents**:
  - `PagedResponseDTO` - Generic pagination wrapper
- **Usage**: Imported as Maven dependency in Auth and User services

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+ (for BFF service)
- pnpm (for BFF service)

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
```

- Frontend communicates only with BFF Service
- BFF Service aggregates data from microservices
- Auth Service creates users in User Service during registration (internal endpoint)
- Services validate JWT tokens independently using shared RSA public key
- Services communicate via HTTP REST APIs

## Security

### JWT Authentication
- **Algorithm**: RS256 (RSA asymmetric encryption)
- **Key Management**: Private key in Auth Service, public key shared with User Service
- **Token Types**: Access token (15min) + Refresh token (7 days)
- **Validation**: Each service validates tokens independently using OAuth2 Resource Server

### Endpoints Security
- **Public**: `/internal/**`, `/actuator/**`, `/swagger-ui/**`
- **Protected**: All other endpoints require valid JWT token
- **CORS**: Configured for `http://localhost:5173` and `http://localhost:3000`

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

## API Documentation

- **Auth Service**: http://localhost:8080/swagger-ui.html
- **User Service**: http://localhost:8081/swagger-ui.html