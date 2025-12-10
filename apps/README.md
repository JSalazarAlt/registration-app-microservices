# Apps

This directory contains all frontend applications for the Registration App.

## 🚀 Technology Stack

- **React** - User interface library
- **JavaScript (ES6+)** - Modern JavaScript features
- **CSS3** - Styling and responsive design
- **Vite** - Fast build tool and development server

## 📁 Project Structure

```
web-app/
├── public/           # Static assets
├── src/
│   ├── components/   # React components
│   │   ├── Login.jsx
│   │   ├── Register.jsx
│   │   ├── Home.jsx
│   │   ├── Auth.css
│   │   └── Home.css
│   ├── App.jsx      # Main application component
│   ├── App.css      # Global styles
│   ├── index.css    # Base styles
│   └── main.jsx     # Application entry point
├── package.json     # Dependencies and scripts
└── vite.config.js   # Vite configuration
```

## 🎨 Features

- **Professional UI** - Modern gradient design with card layouts
- **Responsive Design** - Works on desktop and mobile devices
- **Form Validation** - Real-time input validation with error messages
- **Authentication Flow** - Seamless login/register switching
- **Loading States** - User feedback during API operations
- **State Management** - React hooks for application state

## 🔧 Getting Started

### Prerequisites
- Node.js 16 or higher
- npm or yarn

### Installation
```bash
cd web-app
npm install
```

### Development
```bash
npm run dev
```
The application will be available at `http://localhost:5173`

### Build for Production
```bash
npm run build
```

### Preview Production Build
```bash
npm run preview
```

## 🌐 API Integration

The frontend communicates with the BFF Service at `http://localhost:3001`:

- **Authentication**: `/api/auth/*`
- **User Management**: `/api/users/*`
- **Profile Data**: `/api/profile/*`

## 📱 Components

### Authentication Components
- **Login.jsx** - User login form with validation
- **Register.jsx** - User registration form with comprehensive validation
- **Auth.css** - Shared styles for authentication components

### Application Components
- **Home.jsx** - Welcome dashboard for authenticated users
- **Home.css** - Styles for home page and dashboard
- **App.jsx** - Main application with routing and state management

## 🎯 User Flow

1. **Landing** - User sees login form by default
2. **Registration** - Switch to registration form if needed
3. **Authentication** - Login with credentials or OAuth2
4. **Dashboard** - Welcome page with aggregated user profile (account + user data)
5. **Profile Management** - View and edit profile information
6. **Logout** - Secure session termination with token cleanup

## 🔒 Security Features

- JWT token storage in localStorage
- Automatic token cleanup on logout
- Protected routes based on authentication state
- Input validation and sanitization
- CORS-enabled API communication
- OAuth2 Google integration
- Real-time form validation with error messages

## 🌐 Port Configuration

| Service | Port | URL |
|---------|------|-----|
| Frontend | 5173 | http://localhost:5173 |
| BFF Service | 3001 | http://localhost:3001 |
| Auth Service | 8080 | http://localhost:8080 |
| User Service | 8081 | http://localhost:8081 |

## 🐳 Docker Support

### Build
```bash
cd web-app
docker build -t web-app:latest .
```

### Run
```bash
docker run -p 80:80 web-app:latest
```

Multi-stage build with:
- Build stage: Node.js 18-alpine
- Production stage: nginx:alpine