import React, { useContext } from 'react';
import { AuthContext } from '../../../../context/AuthContext';
import { useNavigate } from 'react-router-dom';

function Menu() {
    const { logout } = useContext(AuthContext)
    const navigate = useNavigate();

    const items = [
        {
            key: 'welcome',
            label: (
                <p
                    style={{
                        background: 'none',
                        border: 'none',
                        width: '100%',
                        textAlign: 'left',
                        padding: '6px 12px',
                        width: '160px'
                    }}
                    onClick={() => navigate('/profile')}
                    className="text-secondary border-bottom border-gray"
                >
                    Chào <br />
                    Anh Duy 😚
                </p>
            ),
        },
        {
            key: 'profile',
            label: (
                <button
                    style={{
                        background: 'none',
                        border: 'none',
                        width: '100%',
                        textAlign: 'left',
                        padding: '6px 12px',
                    }}
                    onClick={() => navigate('/profile')}
                    className="fw-bold"
                >
                    <i className="fa-solid fa-user me-2"></i>
                    Tài khoản
                </button>
            ),
        },
        {
            key: 'logout',
            label: (
                <button
                    style={{
                        background: 'none',
                        border: 'none',
                        width: '100%',
                        textAlign: 'left',
                        padding: '6px 12px',
                        color: 'red',
                    }}
                    onClick={logout}
                    className="fw-bold"
                >
                    <i className="fa-solid fa-arrow-right-from-bracket me-2"></i>
                    Đăng xuất
                </button>
            ),
        },
    ];
    return items;
}


export default Menu

