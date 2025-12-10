# Frontend - Registration App

React-based frontend application for user authentication and profile management.

## 🚀 Technology Stack

- **React** - User interface library
- **JavaScript (ES6+)** - Modern JavaScript features
- **CSS3** - Styling with gradient designs and responsive layout
- **Vite** - Fast build tool and development server with HMR

## 📚 Features

- **User Registration** - Create new accounts with validation
- **User Login** - Authenticate with JWT tokens
- **OAuth2 Google Login** - Seamless Google authentication
- **Profile Management** - View and edit user information
- **Responsive Design** - Works on desktop and mobile devices
- **Real-time Validation** - Client-side form validation with error messages
- **Loading States** - User feedback during API operations
- **Professional UI** - Modern gradient backgrounds and card layouts

## 📝 Project Structure

```
frontend/
├── public/           # Static assets
├── src/
│   ├── components/   # React components
│   │   ├── Login.jsx      # Login form component
│   │   ├── Register.jsx   # Registration form component
│   │   ├── Home.jsx       # Dashboard component
│   │   ├── Auth.css       # Authentication styles
│   │   └── Home.css       # Dashboard styles
│   ├── App.jsx       # Main application component
│   ├── App.css       # Global styles
│   ├── index.css     # Base styles
│   └── main.jsx      # Application entry point
├── package.json      # Dependencies and scripts
└── vite.config.js    # Vite configuration
```

## 🔧 Setup

### Prerequisites
- Node.js 16+
- npm

### Installation
```bash
cd frontend
npm install
```

### Development
```bash
npm run dev
```
Application runs at `http://localhost:5173`

### Build for Production
```bash
npm run build
```

### Preview Production Build
```bash
npm run preview
```

## 🎯 User Flow

1. **Landing** - Login form displayed by default
2. **Registration** - Switch to registration form if needed
3. **Authentication** - Login with credentials or Google OAuth2
4. **Dashboard** - Welcome page with aggregated user profile
5. **Profile Management** - View and edit profile information
6. **Logout** - Secure session termination with token cleanup

## 🔗 API Integration

Communicates with BFF Service at `http://localhost:3001`:

- **Authentication**: `/api/auth/*`
- **User Management**: `/api/users/*`
- **Profile Data**: `/api/profile/*` (aggregated account + user data)

## 🔒 Security

- JWT token storage in localStorage
- Automatic token cleanup on logout
- Protected routes based on authentication state
- Input validation and sanitization
- CORS-enabled API communication

## 🌐 Port Configuration

| Service | Port | URL |
|---------|------|-----|
| Frontend | 5173 | http://localhost:5173 |
| BFF Service | 3001 | http://localhost:3001 |
| Auth Service | 8080 | http://localhost:8080 |
| User Service | 8081 | http://localhost:8081 |

## 🎨 Components

### Login.jsx
- Email/password authentication
- Google OAuth2 button
- Form validation with error messages
- Loading state during authentication

### Register.jsx
- Comprehensive registration form
- Real-time validation (email, password strength, phone format)
- Terms and privacy policy acceptance
- Error handling and user feedback

### Home.jsx
- Welcome dashboard for authenticated users
- Displays aggregated profile data (account + user info)
- Profile edit functionality
- Logout button

## 🐳 Docker Support

### Build
```bash
docker build -t frontend:latest .
```

### Run
```bash
docker run -p 80:80 frontend:latest
```

Multi-stage build:
- Build stage: Node.js 18-alpine with Vite
- Production stage: nginx:alpine for serving static files

## 👤 Author

**Joel Salazar**
- Email: ed.joel.salazar@gmail.com
