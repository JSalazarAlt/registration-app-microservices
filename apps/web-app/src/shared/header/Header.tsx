import { useNavigate } from 'react-router-dom';
import { useState, useRef, useEffect } from 'react';
import './Header.css';

export default function Header() {
    const navigate = useNavigate();
    const [open, setOpen] = useState(false);
    const ref = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const close = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) {
                setOpen(false);
            }
        };
        document.addEventListener('mousedown', close);
        return () => document.removeEventListener('mousedown', close);
    }, []);

    const handleLogout = async () => {
        const refreshToken = localStorage.getItem('refreshToken');
        const accessToken = localStorage.getItem('accessToken');
        try {
            if (refreshToken) {
                await fetch('http://localhost:3001/api/v1/auth/logout', {
                    method: 'POST',
                    headers: { 
                        'Content-Type': 'application/json',
                        Authorization: `Bearer ${accessToken}`,
                     },
                    body: JSON.stringify({ refreshToken }),
                });
                console.log('Refresh token:', refreshToken)
            }
        } catch (err) {
            console.error('Logout error:', err);
        } finally {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            navigate('/login', { replace: true });
        }
    };

    return (
        <header className="header">
            <div className="logo">Suyos</div>

            <div className="avatar-wrapper" ref={ref}>
                <img
                    className="avatar"
                    src="/avatar.png"
                    onClick={() => setOpen(!open)}
                />

                {open && (
                    <div className="dropdown">
                        <button onClick={() => navigate('/profile')}>
                            Profile
                        </button>
                        <button onClick={() => navigate('/sessions')}>
                            Sessions
                        </button>
                        <button className="danger" onClick={handleLogout}>
                            Logout
                        </button>
                    </div>
                )}
            </div>
        </header>
    );
}