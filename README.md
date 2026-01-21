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
- **Event-Driven Architecture** - Kafka for async communication between services
- **Centralized Logging** - ELK Stack (Elasticsearch, Logstash, Kibana)
- **Monitoring & Metrics** - Prometheus and Grafana integration
- **Containerization** - Docker support with multi-stage builds
- **Orchestration** - Kubernetes deployment manifests
- **Professional UI** - Modern gradient design with responsive layout
- **Security Features** - Account locking, failed login tracking, password encryption
- **Real-time Validation** - Client-side form validation with error handling
- **Error Documentation** - Comprehensive error documentation for all exceptions

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
- **Spring Kafka** - Event-driven async communication
- **JWT (JSON Web Tokens)** - Stateless authentication with RS256 (RSA)
- **MapStruct** - Object mapping between DTOs and entities
- **Lombok** - Reduces boilerplate code with annotations
- **Maven** - Dependency management and build tool
- **Swagger/OpenAPI** - API documentation and testing
- **MySQL** - Relational database
- **Logback** - Structured logging with Logstash encoder

### BFF Service
- **NestJS** - Node.js framework for Backend-for-Frontend
- **TypeScript** - Type-safe JavaScript
- **Axios** - HTTP client for microservice communication

### Shared Libraries
- **Common Library** - Shared DTOs (PagedResponseDTO, Events) across microservices

### Infrastructure
- **Kafka** - Message broker for event streaming
- **Elasticsearch** - Search and analytics engine for logs
- **Logstash** - Log aggregation and processing
- **Kibana** - Log visualization and analysis
- **Prometheus** - Metrics collection and monitoring
- **Grafana** - Metrics visualization and dashboards
- **Docker** - Container platform with multi-stage builds
- **Kubernetes** - Container orchestration with health checks

## 📋 Prerequisites

- **Node.js** (v16 or higher)
- **Java** (JDK 17 or higher)
- **Maven** (v3.6 or higher)
- **pnpm** (for BFF service)

## 🏗️ Architecture

```
Frontend (React:5173) → BFF Service (NestJS:3001) → Auth Service (Spring Boot:8080)
                                                   → User Service (Spring Boot:8081)
                                                   
                        Shared Library (common:1.0.0) ← Auth Service
                                                       ← User Service

                        Kafka Topics:
                        - user-creation
                        - account-username-update
                        - account-email-update
                        
                        Auth Service → Kafka → User Service (async event processing)
                        
                        ELK Stack:
                        Auth/User Services → Logstash:5000 → Elasticsearch → Kibana:5601
                        
                        Monitoring:
                        Services → Prometheus:9090 → Grafana:3000
```

### Port Reference Table

| Service | Port | Purpose |
|---------|------|----------|
| Frontend | 5173 | React application (Vite dev server) |
| BFF Service | 3001 | Backend-for-Frontend (NestJS) |
| Auth Service | 8080 | Authentication microservice (Spring Boot) |
| User Service | 8082 | User profile microservice (Spring Boot) |
| Payment Service | 8083 | Payment processing microservice (Spring Boot) |
| MySQL (Auth) | 3306 | Auth Service database |
| MySQL (User) | 3307 | User Service database |
| MySQL (Payment) | 3308 | Payment Service database |
| Kafka | 9092 | Message broker |
| Zookeeper | 2181 | Kafka coordination |
| Elasticsearch | 9200 | Log storage and search |
| Logstash | 5000 | Log aggregation |
| Kibana | 5601 | Log visualization |
| Prometheus | 9090 | Metrics collection |
| Grafana | 3000 | Metrics visualization |
| Jaeger | 16686 | Distributed tracing UI |
| Jaeger OTLP | 4318 | OpenTelemetry collector |

### Key Architectural Decisions
- **JWT Authentication**: RS256 asymmetric encryption (private key in Auth, public key shared)
- **Independent Token Validation**: Each service validates JWT independently using OAuth2 Resource Server
- **Shared Library**: Common DTOs and Events to avoid duplication across microservices
- **BFF Pattern**: Aggregates data from multiple services for simplified frontend consumption
- **Event-Driven**: Kafka for async, non-blocking communication between services
- **Async Processing**: @Async annotation with error handling and retry logic (3 retries, 2s backoff)
- **Centralized Logging**: Structured JSON logs sent to ELK stack for analysis
- **Observability**: Prometheus metrics with Grafana dashboards for monitoring

## 🔧 Installation & Setup

### 1. Infrastructure Setup (Optional)
```bash
# Start Kafka
cd infrastructure
docker-compose up -d

# Start ELK Stack
cd elk
docker-compose -f docker-compose-elk.yml up -d

# Start Monitoring (Prometheus + Grafana)
cd monitoring
docker-compose -f docker-compose-monitoring.yml up -d
```

### 2. Shared Library Setup (Required First)
```bash
cd shared/common
mvn clean install
```

### 3. Auth Service Setup
```bash
cd services/authservice
mvn clean install
mvn spring-boot:run
```

### 4. User Service Setup
```bash
cd services/userservice
mvn clean install
mvn spring-boot:run
```

### 5. BFF Service Setup
```bash
cd services/bffservice
pnpm install
pnpm run start:dev
```

### 6. Frontend Setup
```bash
cd apps/web-app
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
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=user-service-group
```

**BFF Service** (`services/bffservice/.env`):
```env
PORT=3001
AUTH_SERVICE_URL=http://localhost:8080
USER_SERVICE_URL=http://localhost:8081
```

**Infrastructure**:
- Frontend: localhost:5173
- BFF Service: localhost:3001
- Auth Service: localhost:8080
- User Service: localhost:8081
- Kafka: localhost:9092
- Elasticsearch: localhost:9200
- Kibana: localhost:5601
- Logstash: localhost:5000
- Prometheus: localhost:9090
- Grafana: localhost:3000
- Jaeger UI: localhost:16686
- Jaeger OTLP: localhost:4318

## 🌐 API Endpoints

### BFF Service (Port 3001)
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

### User Service (Port 8082)
- `GET /api/v1/users/{userId}` - Get user profile by ID
- `PUT /api/v1/users/{userId}` - Update user profile by ID
- `GET /api/v1/users/account/{accountId}` - Get user profile by account ID
- `PUT /api/v1/users/account/{accountId}` - Update user profile by account ID
- `GET /api/v1/users` - Get all users (paginated)
- `GET /api/v1/users/search` - Search users by name
- `POST /internal/users` - Create user (internal endpoint)

### Payment Service (Port 8083)
- `POST /api/v1/payments/intent` - Create payment intent
- `POST /api/v1/payments/{paymentId}/confirm` - Confirm payment
- `POST /api/v1/payments/{paymentId}/refund` - Refund payment
- `GET /api/v1/payments/{paymentId}` - Get payment by ID
- `GET /api/v1/payments` - Get all payments for current user (paginated)

## 🔐 Security Features

- **Password Encryption** - BCrypt hashing algorithm
- **JWT Tokens** - RS256 (RSA asymmetric) with refresh token rotation
  - Access Token: 15 minutes
  - Refresh Token: 7 days
- **Independent Token Validation** - Each service validates JWT using OAuth2 Resource Server
- **Account Locking** - Automatic lockout after 5 failed attempts for 24 hours
- **OAuth2 Integration** - Google OAuth2 with account linking and active account validation
- **Input Validation** - Comprehensive data validation with Bean Validation
- **CORS Configuration** - Configured for `http://localhost:5173` and `http://localhost:3001`
- **Soft Deletion** - Account and user soft deletion for audit trails
- **Error Documentation** - Comprehensive error docs at `/docs/errors/` for all exceptions

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
- Kafka event producer (user-creation, account-username-update, account-email-update)
- Structured logging to ELK stack
- Prometheus metrics endpoint
- Error documentation: `/docs/errors/`
- Swagger UI: http://localhost:8080/swagger-ui.html

### User Service (Port 8082)
- User profile management
- JWT token validation (OAuth2 Resource Server with shared public key)
- Personal information storage
- Profile picture and preferences
- User search and pagination
- Terms and privacy policy acceptance tracking
- Email/username sync from Auth Service
- User soft deletion
- Kafka event consumer (async processing with @Async, error handling, 3 retries)
- Structured logging to ELK stack
- Prometheus metrics endpoint
- Error documentation: `/docs/errors/`
- Swagger UI: http://localhost:8082/swagger-ui.html

### Payment Service (Port 8083)
- Payment processing with Stripe
- Payment intent creation and confirmation
- Payment refunds
- Transaction history tracking
- JWT token validation (OAuth2 Resource Server)
- Kafka event producer (payment-completed, payment-failed, refund-processed)
- Structured logging to ELK stack
- Prometheus metrics endpoint
- Error documentation: `/docs/errors/`
- Swagger UI: http://localhost:8083/swagger-ui.html

### BFF Service (Port 3001)
- Data aggregation from Auth and User services
- Simplified frontend API
- Request routing and transformation
- Complete profile endpoint (account + user data)
- Error handling and transformation
- OpenTelemetry distributed tracing
- Winston logging with Logstash integration

### Shared Library (common:1.0.0)
- **Location**: `shared/common`
- **Contents**: 
  - PagedResponseDTO for pagination
  - Event DTOs (UserCreationEvent, AccountUsernameUpdateEvent, AccountEmailUpdateEvent)
  - ApiException base class
  - ErrorCode enum
- **Usage**: Maven dependency in Auth and User services
- **Structure**: 
  - `com.suyos.common.dto.response.PagedResponseDTO`
  - `com.suyos.common.event.*`
  - `com.suyos.common.exception.*`

## 📊 API Documentation

- **Auth Service Swagger**: http://localhost:8080/swagger-ui.html
- **User Service Swagger**: http://localhost:8082/swagger-ui.html
- **Payment Service Swagger**: http://localhost:8083/swagger-ui.html
- **Auth Service Error Docs**: `/services/core/auth-service/docs/errors/`
- **User Service Error Docs**: `/services/core/user-service/docs/errors/`
- **Payment Service Error Docs**: `/services/core/payment-service/docs/errors/`

## 📈 Monitoring & Logging

- **Kibana (Logs)**: http://localhost:5601
  - Index Pattern: `logs-*`
  - Timestamp Field: `@timestamp`
- **Prometheus (Metrics)**: http://localhost:9090
- **Grafana (Dashboards)**: http://localhost:3000
  - Default credentials: admin/admin

## 🐳 Docker & Kubernetes

### Docker Build
```bash
# See BUILD.md for detailed build commands
cd services/authservice && docker build -t authservice:latest .
cd services/userservice && docker build -t userservice:latest .
cd services/bffservice && docker build -t bffservice:latest .
cd apps/web-app && docker build -t web-app:latest .
```

### Kubernetes Deployment
```bash
# Apply all manifests
kubectl apply -f kubernetes/

# Check deployments
kubectl get deployments
kubectl get pods
kubectl get services
```

## 📚 Additional Documentation

- **BUILD.md** - Docker build commands and quick rebuild scripts
- **services/core/auth-service/docs/errors/** - Auth service error documentation
- **services/core/user-service/docs/errors/** - User service error documentation
- **services/core/payment-service/docs/errors/** - Payment service error documentation
- **MISSING_TESTS.md** - Missing test types and examples

## 📄 License

This project is licensed under the MIT License.

## 👨💻 Author

**Joel Salazar**
- Email: ed.joel.salazar@gmail.com

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

## ⭐ Show your support

Give a ⭐️ if this project helped you!