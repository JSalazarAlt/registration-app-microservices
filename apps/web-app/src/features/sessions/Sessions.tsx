
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../../shared/header/Header';
import api from '../../utils/auth';
import './Sessions.css';

interface Session {
    id: string;
    deviceName: string;
    userAgent: string;
    ipAddress: string;
    createdAt: string;
}

interface SessionsData {
    sessions: Session[];
}

export default function Session() {
    const navigate = useNavigate();
    const [data, setData] = useState<SessionsData | null>(null);
    const [loading, setLoading] = useState(true);

    const handleGlobalLogout = async () => {
        const token = localStorage.getItem('accessToken');

        if (!token) {
            navigate('/login', { replace: true });
            return;
        }

        try {
            // Server reads refresh token from HttpOnly cookie; include credentials to send cookie
            await fetch('http://localhost:8080/api/v1/sessions/me', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                credentials: 'include',
                body: JSON.stringify({}),
            });
            console.log('Global logout requested (refresh token is stored in HttpOnly cookie)');
        } catch (err) {
            console.error('Global logout error:', err);
        } finally {
            // Always clear local auth state
            localStorage.removeItem('accessToken');
            // refresh token is stored in an HttpOnly cookie; do not access/remove from JS
            navigate('/login', { replace: true });
        }
    };

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (!token) {
            navigate('/login', { replace: true });
            return;
        }

        const fetchProfile = async () => {
            try {
                const res = await api.fetchWithAuth('http://localhost:8080/api/v1/sessions/me');

                if (!res.ok) {
                    navigate('/login', { replace: true });
                    return;
                }

                const json = await res.json();
                setData(json);
            } catch (err) {
                console.error('Profile fetch error:', err);
                navigate('/login', { replace: true });
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, [navigate]);

    if (loading) {
        return (
            <>
                <Header />
                <div className="page">Loading profile…</div>
            </>
        );
    }

    if (!data) {
        return (
            <>
                <Header />
                <div className="page">Failed to load profile</div>
            </>
        );
    }

    const { sessions } = data;

    return (
        <>
            <Header />

            <div className="page profile-page">
                <h1>Activity</h1>

                <section className="card">
                    <div className="card-header">
                        <h2>Active Sessions</h2>
                        <button
                            className="logout-all-btn"
                            onClick={handleGlobalLogout}
                        >
                            Log out from all devices
                        </button>
                    </div>
                    <table>
                        <thead>
                            <tr>
                                <th>Device</th>
                                <th>IP</th>
                                <th>Created</th>
                            </tr>
                        </thead>
                        <tbody>
                            {sessions.map((s) => (
                                <tr key={s.id}>
                                    <td>{s.userAgent || 'Unknown'}</td>
                                    <td>{s.ipAddress}</td>
                                    <td>{new Date(s.createdAt).toLocaleString()}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </section>
            </div>
        </>
    );
}