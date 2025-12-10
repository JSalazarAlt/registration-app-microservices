import { useState, useEffect } from 'react';
import Login from './components/Login';
import Register from './components/Register';
import Home from './components/Home';
import Profile from './components/Profile';
import Admin from './components/Admin';
import './App.css';

function App() {
  const [currentView, setCurrentView] = useState('login');
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    // Check if user is already authenticated
    const token = localStorage.getItem('accessToken');
    if (token) {
      setIsAuthenticated(true);
      setCurrentView('home');
    }
  }, []);

  const handleLoginSuccess = () => {
    setIsAuthenticated(true);
    setCurrentView('home');
  };

  const handleRegisterSuccess = () => {
    setCurrentView('login');
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setCurrentView('login');
  };

  const switchToRegister = () => {
    setCurrentView('register');
  };

  const switchToLogin = () => {
    setCurrentView('login');
  };

  if (isAuthenticated) {
    if (currentView === 'home') {
      return <Home onLogout={handleLogout} onNavigate={setCurrentView} />;
    }
    if (currentView === 'profile') {
      return <Profile onLogout={handleLogout} />;
    }
    if (currentView === 'admin') {
      return <Admin onLogout={handleLogout} />;
    }
  }

  if (currentView === 'register') {
    return (
      <Register 
        onSwitchToLogin={switchToLogin}
        onRegisterSuccess={handleRegisterSuccess}
      />
    );
  }

  return (
    <Login 
      onSwitchToRegister={switchToRegister}
      onLoginSuccess={handleLoginSuccess}
    />
  );
}

export default App
