# BFF Service (Backend-for-Frontend)

NestJS-based Backend-for-Frontend service that aggregates data from Auth and User microservices.

## 🚀 Overview

- **Port**: 3001
- **Technology**: NestJS, TypeScript, Axios
- **Purpose**: Simplify frontend API consumption by aggregating microservice data

## 🎯 Features

- **Data Aggregation**: Combines Auth and User service responses
- **Request Routing**: Forwards requests to appropriate microservices
- **Simplified API**: Single endpoint for complete user profile (account + user data)
- **Error Handling**: Centralized error handling and transformation
- **CORS Configuration**: Configured for frontend at `http://localhost:5173`

## 📚 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/refresh` - Refresh JWT token
- `GET /api/auth/accounts/{username}` - Get account by username

### User Management
- `GET /api/users/profile/{accountId}` - Get user profile
- `PUT /api/users/profile/{accountId}` - Update user profile

### Aggregated Data
- `GET /api/profile/{username}` - Get complete profile (account + user data)

## 🔧 Setup

### Prerequisites
- Node.js 16+
- pnpm

### Installation
```bash
pnpm install
```

### Environment Variables
Create `.env` file:
```env
AUTH_SERVICE_URL=http://localhost:8080
USER_SERVICE_URL=http://localhost:8081
```

## 🛠️ Development

```bash
# Development mode with hot reload
pnpm run start:dev

# Production mode
pnpm run start:prod

# Build
pnpm run build
```

## 🧪 Testing

```bash
# Unit tests
pnpm run test

# E2E tests
pnpm run test:e2e

# Test coverage
pnpm run test:cov
```

## 📝 Project Structure

```
src/
├── auth/           # Auth module (forwards to Auth Service)
├── users/          # Users module (forwards to User Service)
├── profile/        # Profile module (aggregates Auth + User data)
├── app.module.ts   # Root module
└── main.ts         # Application entry point
```

## 🔗 Service Communication

```
Frontend (5173) → BFF Service (3000) → Auth Service (8080)
                                      → User Service (8081)

Auth Service → Kafka → User Service (async events)
```

## 🐳 Docker Support

### Build
```bash
docker build -t bffservice:latest .
```

### Run
```bash
docker run -p 3000:3000 bffservice:latest
```

Multi-stage build with Node.js 18-alpine

## 📚 Additional Features

- Error transformation and handling
- Request/response logging
- CORS configuration for frontend
- Environment-based configuration
- Health check endpoints

## 👤 Author

**Joel Salazar**
- Email: ed.joel.salazar@gmail.com
