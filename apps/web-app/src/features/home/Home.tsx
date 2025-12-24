import { useNavigate } from 'react-router-dom';
import Header from '../../shared/header/Header';
import './Home.css';

export default function Home() {
    const navigate = useNavigate();

    return (
        <>
            <Header />
            <main className="home">
                <h1>Welcome</h1>

                <div className="cards">
                    <div className="card" onClick={() => navigate('/profile')}>
                        Profile
                    </div>
                    <div className="card" onClick={() => navigate('/sessions')}>
                        Sessions
                    </div>
                </div>
            </main>
        </>
    );
}