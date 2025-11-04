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

### Backend
- **Spring Boot** - Java framework for microservices
- **Spring Security** - Authentication and authorization
- **Spring OAuth2** - Google OAuth2 integration
- **Spring Data JPA** - Database operations and ORM
- **JWT (JSON Web Tokens)** - Stateless authentication
- **MapStruct** - Object mapping between DTOs and entities
- **Lombok** - Reduces boilerplate code with annotations
- **Maven** - Dependency management and build tool
- **Swagger/OpenAPI** - API documentation and testing

### BFF Service
- **NestJS** - Node.js framework for Backend-for-Frontend
- **TypeScript** - Type-safe JavaScript
- **Axios** - HTTP client for microservice communication

## 📋 Prerequisites

- **Node.js** (v16 or higher)
- **Java** (JDK 17 or higher)
- **Maven** (v3.6 or higher)
- **pnpm** (for BFF service)

## 🏗️ Architecture

```
Frontend (React) → BFF Service (NestJS) → Microservices (Spring Boot)
                                        ├── Auth Service (Port 8080)
                                        └── User Service (Port 8081)
```

## 🔧 Installation & Setup

### Auth Service Setup
```bash
cd services/authservice
mvn clean install
mvn spring-boot:run
```

### User Service Setup
```bash
cd services/userservice
mvn clean install
mvn spring-boot:run
```

### BFF Service Setup
```bash
cd services/bffservice
pnpm install
pnpm run start:dev
```

### Frontend Setup
```bash
cd web/frontend
npm install
npm run dev
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
- **JWT Tokens** - Secure stateless authentication with refresh token rotation
- **Account Locking** - Automatic lockout after 5 failed attempts for 24 hours
- **OAuth2 Integration** - Google OAuth2 with account linking
- **Input Validation** - Comprehensive data validation with Bean Validation
- **CORS Configuration** - Cross-origin resource sharing setup
- **Active Account Validation** - Enhanced OAuth2 security with active account checks

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

### Auth Service
- Account management and authentication
- JWT token generation and validation
- OAuth2 integration with Google
- Account security features (locking, failed attempts)
- Password encryption and validation

### User Service
- User profile management
- Personal information storage
- Profile picture and preferences
- User search and pagination
- Terms and privacy policy acceptance tracking

### BFF Service
- Data aggregation from multiple microservices
- Simplified frontend API
- Request routing and transformation
- Cross-service data composition

## 📄 License

This project is licensed under the MIT License.

## 👨💻 Author

**Joel Salazar**
- Email: ed.joel.salazar@gmail.com

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

## ⭐ Show your support

Give a ⭐️ if this project helped you!