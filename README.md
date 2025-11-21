# Registration App v2.0.0

A full-stack authentication application built with React, Spring Boot microservices, and NestJS BFF, featuring user registration, login, JWT-based authentication, and OAuth2 integration.

## 🚀 Features

- **User Registration** - Create new accounts with comprehensive validation
- **User Login** - Secure authentication with JWT tokens
- **OAuth2 Authentication** - Google OAuth2 integration for seamless login
- **Profile Management** - View and edit user profile information
- **JWT Authentication** - Stateless authentication using JSON Web Tokens
- **Microservices Architecture** - Separate Auth and User services
- **BFF Pattern** - Backend-for-Frontend service for data aggregation
- **Professional UI** - Modern gradient design with responsive layout
- **Security Features** - Account locking, failed login tracking, password encryption
- **Real-time Validation** - Client-side form validation with error handling

## 🛠️ Tech Stack

### Frontend
- **React** - User interface library
- **JavaScript (ES6+)** - Modern JavaScript features
- **CSS3** - Styling and responsive design
- **Vite** - Fast build tool and development server

### Backend Microservices
- **Spring Boot** - Java framework for microservices
- **Spring Security** - Authentication and authorization
- **Spring OAuth2 Resource Server** - JWT validation
- **Spring OAuth2 Client** - Google OAuth2 integration
- **Spring Data JPA** - Database operations and ORM
- **JWT (JSON Web Tokens)** - Stateless authentication with RS256 (RSA)
- **MapStruct** - Object mapping between DTOs and entities
- **Lombok** - Reduces boilerplate code with annotations
- **Maven** - Dependency management and build tool
- **Swagger/OpenAPI** - API documentation and testing
- **MySQL** - Relational database

### BFF Service
- **NestJS** - Node.js framework for Backend-for-Frontend
- **TypeScript** - Type-safe JavaScript
- **Axios** - HTTP client for microservice communication

### Shared Libraries
- **Common Library** - Shared DTOs (PagedResponseDTO) across microservices

## 📋 Prerequisites

- **Node.js** (v16 or higher)
- **Java** (JDK 17 or higher)
- **Maven** (v3.6 or higher)
- **pnpm** (for BFF service)

## 🏗️ Architecture

```
Frontend (React:5173) → BFF Service (NestJS:3000) → Auth Service (Spring Boot:8080)
                                                   → User Service (Spring Boot:8081)
                                                   
                        Shared Library (common:1.0.0) ← Auth Service
                                                       ← User Service
```

### Key Architectural Decisions
- **JWT Authentication**: RS256 asymmetric encryption (private key in Auth, public key shared)
- **Independent Token Validation**: Each service validates JWT independently using OAuth2 Resource Server
- **Shared Library**: Common DTOs to avoid duplication across microservices
- **BFF Pattern**: Aggregates data from multiple services for simplified frontend consumption

## 🔧 Installation & Setup

### 1. Shared Library Setup (Required First)
```bash
cd shared/common
mvn clean install
```

### 2. Auth Service Setup
```bash
cd services/authservice
mvn clean install
mvn spring-boot:run
```

### 3. User Service Setup
```bash
cd services/userservice
mvn clean install
mvn spring-boot:run
```

### 4. BFF Service Setup
```bash
cd services/bffservice
pnpm install
pnpm run start:dev
```

### 5. Frontend Setup
```bash
cd web/frontend
npm install
npm run dev
```

### Environment Variables

**Auth Service** (`services/authservice/src/main/resources/application.properties`):
```properties
jwt.private-key=classpath:keys/private_key.pem
jwt.public-key=classpath:keys/public_key.pem
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
```

**User Service** (`services/userservice/src/main/resources/application.properties`):
```properties
jwt.public-key=classpath:keys/public_key.pem
```

## 🌐 API Endpoints

### BFF Service (Port 3000)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/refresh` - Refresh JWT token
- `GET /api/auth/accounts/{username}` - Get account by username
- `GET /api/users/profile/{accountId}` - Get user profile
- `PUT /api/users/profile/{accountId}` - Update user profile
- `GET /api/profile/{username}` - Get complete aggregated profile

### Auth Service (Port 8080)
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/logout` - User logout
- `POST /api/v1/auth/refresh` - Refresh JWT token
- `GET /api/v1/accounts/{username}` - Get account by username
- `GET /api/v1/accounts/me` - Get current logged-in account
- `PATCH /api/v1/accounts/me` - Update current logged-in account

### User Service (Port 8081)
- `GET /api/v1/users/{userId}` - Get user profile by ID
- `PUT /api/v1/users/{userId}` - Update user profile by ID
- `GET /api/v1/users/account/{accountId}` - Get user profile by account ID
- `PUT /api/v1/users/account/{accountId}` - Update user profile by account ID
- `GET /api/v1/users` - Get all users (paginated)
- `GET /api/v1/users/search` - Search users by name
- `POST /internal/users` - Create user (internal endpoint)

## 🔐 Security Features

- **Password Encryption** - BCrypt hashing algorithm
- **JWT Tokens** - RS256 (RSA asymmetric) with refresh token rotation
  - Access Token: 15 minutes
  - Refresh Token: 7 days
- **Independent Token Validation** - Each service validates JWT using OAuth2 Resource Server
- **Account Locking** - Automatic lockout after 5 failed attempts for 24 hours
- **OAuth2 Integration** - Google OAuth2 with account linking and active account validation
- **Input Validation** - Comprehensive data validation with Bean Validation
- **CORS Configuration** - Configured for `http://localhost:5173` and `http://localhost:3000`
- **Soft Deletion** - Account and user soft deletion for audit trails

## 📱 Application Flow

1. **Registration** - Users create accounts with comprehensive validation
2. **Login** - Authentication with JWT token generation or Google OAuth2
3. **Home Page** - Welcome dashboard for authenticated users with aggregated profile data
4. **Profile Management** - View and edit user information through BFF service
5. **Logout** - Secure session termination with refresh token revocation

## 🎨 UI/UX Features

- **Professional Design** - Modern gradient backgrounds and card layouts
- **Responsive Layout** - Works seamlessly on desktop and mobile devices
- **Form Validation** - Real-time input validation with detailed error messages
- **Loading States** - User feedback during API operations
- **Authentication Flow** - Seamless login/register switching with state management

## 🏛️ Microservices Design

### Auth Service (Port 8080)
- Account management and authentication
- JWT token generation (RS256 with private key)
- JWT token validation (OAuth2 Resource Server)
- OAuth2 integration with Google
- Account security features (locking after 5 failed attempts, 24h lockout)
- Password encryption with BCrypt
- Refresh token rotation
- Account soft deletion
- Swagger UI: http://localhost:8080/swagger-ui.html

### User Service (Port 8081)
- User profile management
- JWT token validation (OAuth2 Resource Server with shared public key)
- Personal information storage
- Profile picture and preferences
- User search and pagination
- Terms and privacy policy acceptance tracking
- Email/username sync from Auth Service
- User soft deletion
- Swagger UI: http://localhost:8081/swagger-ui.html

### BFF Service (Port 3000)
- Data aggregation from Auth and User services
- Simplified frontend API
- Request routing and transformation
- Complete profile endpoint (account + user data)
- Error handling and transformation

### Shared Library (common:1.0.0)
- **Location**: `shared/common`
- **Contents**: PagedResponseDTO for pagination
- **Usage**: Maven dependency in Auth and User services
- **Structure**: `com.suyos.common.dto.response.PagedResponseDTO`

## 📊 API Documentation

- **Auth Service Swagger**: http://localhost:8080/swagger-ui.html
- **User Service Swagger**: http://localhost:8081/swagger-ui.html

## 📄 License

This project is licensed under the MIT License.

## 👨💻 Author

**Joel Salazar**
- Email: ed.joel.salazar@gmail.com

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

## ⭐ Show your support

Give a ⭐️ if this project helped you!