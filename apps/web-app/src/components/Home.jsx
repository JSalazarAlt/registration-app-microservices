import { useState, useEffect } from 'react';
import './Home.css';

const Home = ({ onLogout, onNavigate }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          onLogout();
          return;
        }

        // Fetch user profile from BFF service
        const response = await fetch('http://localhost:3001/api/profile/user', {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });
        
        if (response.ok) {
          const profileData = await response.json();
          setUser({
            username: profileData.account.username,
            firstName: profileData.profile.firstName,
            lastName: profileData.profile.lastName,
            email: profileData.account.email
          });
        } else {
          // Fallback to mock data if profile fetch fails
          setUser({
            username: 'user@example.com',
            firstName: 'John',
            lastName: 'Doe'
          });
        }
        setLoading(false);
      } catch (error) {
        console.error('Failed to fetch user data:', error);
        onLogout();
      }
    };

    fetchUserData();
  }, [onLogout]);

  const handleLogout = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        await fetch('http://localhost:3001/api/auth/logout', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ refreshToken }),
        });
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      onLogout();
    }
  };

  if (loading) {
    return (
      <div className="home-container">
        <div className="loading-spinner">Loading...</div>
      </div>
    );
  }

  return (
    <div className="home-container">
      <header className="home-header">
        <div className="header-content">
          <h1>Suyos Platform</h1>
          <div className="user-menu">
            <span className="user-greeting">
              Welcome, {user?.firstName || 'User'}!
            </span>
            <button 
              className="logout-button"
              onClick={handleLogout}
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="home-main">
        <div className="welcome-section">
          <div className="welcome-card">
            <h2>Welcome to Suyos Platform</h2>
            <p>
              You have successfully logged into your account. This is your 
              personal dashboard where you can manage your profile and access 
              platform features.
            </p>
            
            <div className="feature-grid">
              <div className="feature-card" onClick={() => onNavigate('profile')} style={{cursor: 'pointer'}}>
                <h3>Profile Management</h3>
                <p>Update your personal information and preferences</p>
              </div>
              
              <div className="feature-card" onClick={() => onNavigate('admin')} style={{cursor: 'pointer'}}>
                <h3>Admin Dashboard</h3>
                <p>Search and manage users in the system</p>
              </div>
              
              <div className="feature-card">
                <h3>Account Activity</h3>
                <p>View your recent login history and activity</p>
              </div>
            </div>
          </div>
        </div>
      </main>

      <footer className="home-footer">
        <p>&copy; 2024 Suyos Platform. All rights reserved.</p>
      </footer>
    </div>
  );
};

export default Home;