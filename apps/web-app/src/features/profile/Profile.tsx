import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../../shared/header/Header';
import api from '../../utils/auth';
import './Profile.css';

interface ProfileData {
    user: {
        firstName: string;
        lastName: string;
        phoneNumber?: string;
    };
    account: {
        username: string;
        email: string;
    };
}

export default function Profile() {
    const navigate = useNavigate();
    const [data, setData] = useState<ProfileData | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (!token) {
            navigate('/login', { replace: true });
            return;
        }

        const fetchProfile = async () => {
            try {
                const res = await api.fetchWithAuth('http://localhost:8080/api/v1/profile/me');

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

    const { user, account } = data;

    return (
        <>
            <Header />

            <div className="page profile-page">
                <h1>Profile</h1>

                <section className="card">
                    <h2>Account</h2>
                    <p><strong>Username:</strong> {account.username}</p>
                    <p><strong>Email:</strong> {account.email}</p>
                </section>

                <section className="card">
                    <h2>Personal Information</h2>
                    <p><strong>Name:</strong> {user.firstName} {user.lastName}</p>
                    <p><strong>Phone:</strong> {user.phoneNumber || '—'}</p>
                </section>

            </div>
        </>
    );
}