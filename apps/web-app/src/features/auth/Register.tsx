import { useNavigate } from 'react-router-dom';
import './Auth.css';

export default function Register() {
    const navigate = useNavigate();

    return (
        <div className="auth">
            <h1>Register</h1>
            <button onClick={() => navigate('/login')}>Back to login</button>
        </div>
    );
}