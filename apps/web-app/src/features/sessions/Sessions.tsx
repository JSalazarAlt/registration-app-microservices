
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../../shared/header/Header';
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
        const refreshToken = localStorage.getItem('refreshToken');

        if (!token || !refreshToken) {
            navigate('/login', { replace: true });
            return;
        }

        try {
            await fetch('http://localhost:3001/api/v1/auth/global-logout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ value: refreshToken }),
            });
        } catch (err) {
            console.error('Global logout error:', err);
        } finally {
            // Always clear local auth state
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
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
                const res = await fetch('http://localhost:3001/api/v1/sessions/me', {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });

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