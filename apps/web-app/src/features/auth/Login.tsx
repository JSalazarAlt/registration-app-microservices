import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Auth.css';

export default function Login() {
    const [identifier, setIdentifier] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const submit = async (e: FormEvent) => {
        e.preventDefault();

        const deviceName = 'Chrome on Desktop';

        console.log('Submitting login', { identifier, password, deviceName });

        const res = await fetch('http://localhost:3001/api/v1/auth/login/web', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ identifier, password, deviceName }),
        });

        if (!res.ok) {
            console.error('Login failed', await res.text());
            return;
        }

        const data = await res.json();
        localStorage.setItem('accessToken', data.accessToken);
        // refreshToken is stored in an HttpOnly cookie by the server; do not store it in localStorage
        navigate('/');
    };

    return (
        <form className="auth" onSubmit={submit}>
            <h1>Sign in</h1>

            <input
                value={identifier}
                placeholder="Email or username"
                onChange={(e) => setIdentifier(e.target.value)}
            />

            <input
                type="password"
                value={password}
                placeholder="Password"
                onChange={(e) => setPassword(e.target.value)}
            />

            <button type="submit">Login</button>

            <span onClick={() => navigate('/register')}>Register</span>
        </form>
    );
}