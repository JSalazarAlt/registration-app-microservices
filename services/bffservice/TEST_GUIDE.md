# BFF Service Testing Guide

## Overview
This guide provides instructions for testing the BFF (Backend-for-Frontend) service with comprehensive unit tests, integration tests, and manual testing procedures.

## Prerequisites
- Node.js v16 or higher
- pnpm package manager
- Auth Service running on port 8080
- User Service running on port 8081

## Setup

### 1. Install Dependencies
```bash
cd services/bffservice
pnpm install
```

### 2. Configure Environment
Create a `.env` file based on `.env.example`:
```bash
cp .env.example .env
```

Edit `.env` with your configuration:
```env
AUTH_SERVICE_URL=http://localhost:8080
USER_SERVICE_URL=http://localhost:8081
REQUEST_TIMEOUT=5000
MAX_RETRIES=3
PORT=3000
```

## Running Tests

### Unit Tests
Run all unit tests with coverage:
```bash
pnpm test
```

Run tests in watch mode (for development):
```bash
pnpm test:watch
```

Generate coverage report:
```bash
pnpm test:cov
```

Coverage report will be generated in `coverage/` directory. Open `coverage/lcov-report/index.html` in a browser to view detailed coverage.

### Run Specific Test Files
```bash
# Test AuthService only
pnpm test auth.service.spec.ts

# Test UserService only
pnpm test user.service.spec.ts

# Test ProfileService only
pnpm test profile.service.spec.ts
```

### End-to-End Tests
Run E2E tests (requires services to be running):
```bash
pnpm test:e2e
```

## Test Structure

### Unit Tests
- **auth.service.spec.ts** - Tests for authentication operations
  - Registration
  - Login
  - Logout
  - Token refresh
  - Account retrieval
  - Error handling (service unavailable, timeout, invalid responses)

- **user.service.spec.ts** - Tests for user profile operations
  - Get user profile
  - Update user profile
  - Get all users (pagination)
  - Search users
  - Error handling (service unavailable, timeout, invalid responses)

- **profile.service.spec.ts** - Tests for aggregated profile operations
  - Get complete profile (account + user data)
  - Error handling for both Auth and User services
  - Service unavailability scenarios
  - Timeout scenarios

## Manual Testing

### 1. Start Required Services
```bash
# Terminal 1: Start Auth Service
cd services/authservice
mvn spring-boot:run

# Terminal 2: Start User Service
cd services/userservice
mvn spring-boot:run

# Terminal 3: Start BFF Service
cd services/bffservice
pnpm run start:dev
```

### 2. Test Endpoints with cURL

#### Register New Account
```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123!",
    "firstName": "Test",
    "lastName": "User",
    "phone": "+1234567890"
  }'
```

#### Login
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser",
    "password": "Password123!"
  }'
```

#### Get Account by Username
```bash
curl -X GET http://localhost:3000/api/auth/accounts/testuser
```

#### Get User Profile
```bash
curl -X GET http://localhost:3000/api/users/profile/{accountId}
```

#### Update User Profile
```bash
curl -X PUT http://localhost:3000/api/users/profile/{accountId} \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Updated",
    "lastName": "Name",
    "phone": "+9876543210"
  }'
```

#### Get Complete Profile (Aggregated)
```bash
curl -X GET http://localhost:3000/api/profile/testuser
```

#### Logout
```bash
curl -X POST http://localhost:3000/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "value": "your-refresh-token-here"
  }'
```

### 3. Test with Postman
Import the following collection structure:

**BFF Service Collection**
- Auth
  - POST Register
  - POST Login
  - POST Logout
  - POST Refresh Token
  - GET Account by Username
- Users
  - GET User Profile
  - PUT Update User Profile
  - GET All Users
  - GET Search Users
- Profile
  - GET Complete Profile

## Testing Error Scenarios

### 1. Service Unavailable
Stop Auth or User service and test endpoints:
```bash
# Should return 503 Service Unavailable
curl -X GET http://localhost:3000/api/auth/accounts/testuser
```

### 2. Timeout
Set `REQUEST_TIMEOUT=100` in `.env` and restart BFF service:
```bash
# Should return 504 Gateway Timeout for slow services
curl -X GET http://localhost:3000/api/profile/testuser
```

### 3. Invalid Credentials
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser",
    "password": "WrongPassword"
  }'
# Should return 401 Unauthorized
```

### 4. Not Found
```bash
curl -X GET http://localhost:3000/api/auth/accounts/nonexistent
# Should return 404 Not Found
```

## Test Coverage Goals
- **Unit Tests**: 80% coverage minimum
- **Integration Tests**: All critical paths covered
- **Error Handling**: All error scenarios tested

## Expected Test Results

### Unit Tests
```
Test Suites: 3 passed, 3 total
Tests:       24 passed, 24 total
Snapshots:   0 total
Time:        5.123 s
```

### Coverage Report
```
File                  | % Stmts | % Branch | % Funcs | % Lines
----------------------|---------|----------|---------|--------
auth.service.ts       |   95.00 |    90.00 |  100.00 |   95.00
user.service.ts       |   95.00 |    90.00 |  100.00 |   95.00
profile.service.ts    |   95.00 |    90.00 |  100.00 |   95.00
```

## Troubleshooting

### Tests Failing
1. Ensure all dependencies are installed: `pnpm install`
2. Clear Jest cache: `pnpm test --clearCache`
3. Check Node.js version: `node --version` (should be v16+)

### Service Connection Issues
1. Verify Auth Service is running on port 8080
2. Verify User Service is running on port 8081
3. Check `.env` configuration
4. Verify no firewall blocking connections

### Timeout Issues
1. Increase `REQUEST_TIMEOUT` in `.env`
2. Check service response times
3. Verify network connectivity

## Continuous Integration

### GitHub Actions Example
```yaml
name: BFF Service Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: pnpm/action-setup@v2
        with:
          version: 8
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'pnpm'
      - run: pnpm install
      - run: pnpm test:cov
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## Best Practices
1. Run tests before committing code
2. Maintain 80%+ test coverage
3. Test all error scenarios
4. Mock external service calls in unit tests
5. Use E2E tests for integration scenarios
6. Keep tests fast and isolated
7. Use descriptive test names
8. Follow AAA pattern (Arrange, Act, Assert)

## Additional Resources
- [NestJS Testing Documentation](https://docs.nestjs.com/fundamentals/testing)
- [Jest Documentation](https://jestjs.io/docs/getting-started)
- [Supertest Documentation](https://github.com/visionmedia/supertest)
