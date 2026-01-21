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
        const accessToken = localStorage.getItem('accessToken');
        try {
            // Server reads refresh token from HttpOnly cookie; include credentials so cookie is sent
            await fetch('http://localhost:8080/api/v1/auth/logout', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${accessToken}`,
                 },
                credentials: 'include',
                body: JSON.stringify({}),
            });
            console.log('Logout requested (refresh token is stored in HttpOnly cookie)');
        } catch (err) {
            console.error('Logout error:', err);
        } finally {
            localStorage.removeItem('accessToken');
            // refresh token is stored in an HttpOnly cookie; do not access/remove from JS
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