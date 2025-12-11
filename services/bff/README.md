# Backend-for-Frontend (BFF) Services

This directory contains all BFF services built with NestJS.

## What is BFF?

Backend-for-Frontend (BFF) is an architectural pattern where you create separate backend services tailored to specific frontend needs. Each BFF aggregates data from multiple microservices and transforms responses to match frontend requirements.

**Benefits:**
- Reduces frontend complexity (1 request instead of multiple)
- Optimizes data payloads for specific clients (web, mobile, admin)
- Decouples frontend from microservice changes
- Enables frontend-specific caching and error handling

---

## Services

### Web BFF (Port 3001)
**Purpose:** Backend service for web application

**Responsibilities:**
- Aggregate data from Auth and User services
- Transform responses for web frontend consumption
- Handle authentication flow (login, register, logout, refresh)
- Provide simplified API endpoints
- Distributed tracing with OpenTelemetry
- Structured logging to ELK Stack

**Tech Stack:**
- NestJS (Node.js framework)
- TypeScript
- Axios (HTTP client)
- Winston (logging)
- OpenTelemetry (distributed tracing)
- JWT validation

**Key Endpoints:**

**Authentication:**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/refresh` - Refresh JWT token
- `GET /api/auth/accounts/{username}` - Get account by username

**User Management:**
- `GET /api/users/profile/{accountId}` - Get user profile
- `PUT /api/users/profile/{accountId}` - Update user profile
- `GET /api/users` - Get all users (paginated)
- `GET /api/users/search` - Search users by name

**Data Aggregation:**
- `GET /api/profile/{username}` - Get complete profile (account + user data)

---

## Architecture Flow

```
Frontend (React:5173) → Web BFF (3001) → API Gateway (8080) → Microservices
                                                              ├─ Auth Service (8081)
                                                              └─ User Service (8082)
```

**Why BFF calls Gateway instead of direct microservices:**
- Gateway handles infrastructure concerns (rate limiting, circuit breaker, JWT validation)
- BFF handles application concerns (data aggregation, response transformation)
- Separation of concerns and defense in depth

---

## Data Aggregation Example

### Complete Profile Endpoint
**Endpoint:** `GET /api/profile/{username}`

**Aggregates:**
1. Account data from Auth Service (`GET /api/v1/accounts/{username}`)
2. User profile from User Service (`GET /api/v1/users/account/{accountId}`)

**Response:**
```json
{
  "account": {
    "id": "uuid",
    "username": "john_doe",
    "email": "john@example.com",
    "isActive": true,
    "isLocked": false
  },
  "profile": {
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "profilePictureUrl": "https://...",
    "bio": "Software engineer"
  }
}
```

**Benefit:** Frontend makes 1 request instead of 2, reducing latency and complexity.

---

## Running Web BFF

### Prerequisites
- Node.js 16+
- pnpm (package manager)

### Installation
```bash
cd services/bff/web-bff
pnpm install
```

### Environment Variables
Create `.env` file:
```env
PORT=3001
GATEWAY_URL=http://localhost:8080
REQUEST_TIMEOUT=5000
MAX_RETRIES=3

# OpenTelemetry
OTEL_SERVICE_NAME=web-bff
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318

# Logstash
LOGSTASH_HOST=localhost
LOGSTASH_PORT=5000
```

### Development
```bash
pnpm run start:dev
```

### Production
```bash
pnpm run build
pnpm run start:prod
```

### Testing
```bash
# Unit tests
pnpm run test

# E2E tests
pnpm run test:e2e

# Test coverage
pnpm run test:cov
```

---

## Future BFF Services

### Mobile BFF (Planned)
**Port:** 3002
**Purpose:** Optimized for mobile apps (iOS/Android)

**Differences from Web BFF:**
- Smaller payloads (reduced data transfer)
- Mobile-specific endpoints (e.g., `/api/mobile/home`)
- Push notification integration
- Offline sync support

### Admin BFF (Planned)
**Port:** 3003
**Purpose:** Admin dashboard with extended permissions

**Features:**
- User management (list, search, ban, delete)
- System metrics and analytics
- Audit logs and activity tracking
- Bulk operations

---

## Observability

### Logging
- Structured JSON logs with Winston
- Logs sent to Logstash (port 5000)
- Includes trace_id and span_id for correlation
- Log levels: error, warn, info, debug

### Tracing
- OpenTelemetry integration
- Trace propagation to Gateway and microservices
- Jaeger UI: http://localhost:16686

### Monitoring
- Health check: `GET /health`
- Metrics endpoint: `GET /metrics` (if enabled)

---

## Error Handling

BFF transforms microservice errors into frontend-friendly responses:

**Microservice Error:**
```json
{
  "errorCode": "ACCOUNT_NOT_FOUND",
  "message": "Account with username 'john_doe' not found",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

**BFF Response:**
```json
{
  "error": "Account not found",
  "message": "The account you're looking for doesn't exist",
  "statusCode": 404
}
```

---

## Security

- JWT validation on protected endpoints
- Request timeout (5 seconds default)
- Retry logic with exponential backoff (3 retries)
- CORS configuration for frontend origin
- Rate limiting (handled by Gateway)

---

## Best Practices

1. **Keep BFF Thin:** Only aggregation and transformation, no business logic
2. **Cache Strategically:** Cache frequently accessed data (user profiles, settings)
3. **Handle Failures Gracefully:** Return partial data if one service fails
4. **Version APIs:** Use `/v1/`, `/v2/` for breaking changes
5. **Monitor Performance:** Track aggregation latency and error rates
