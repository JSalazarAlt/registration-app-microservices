import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from '../features/auth/Login';
import Register from '../features/auth/Register';
import Home from '../features/home/Home';
import Profile from '../features/profile/Profile';
import Sessions from '../features/sessions/Sessions';
import ProtectedRoute from './ProtectedRoute';

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />

                <Route
                    path="/"
                    element={
                        <ProtectedRoute>
                            <Home />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/profile"
                    element={
                        <ProtectedRoute>
                            <Profile />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/sessions"
                    element={
                        <ProtectedRoute>
                            <Sessions />
                        </ProtectedRoute>
                    }
                />
            </Routes>
        </BrowserRouter>
    );
}