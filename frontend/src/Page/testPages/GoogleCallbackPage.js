import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

const GoogleCallbackPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const [userInfo, setUserInfo] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const code = params.get('code');

        if (code) {
            fetch('http://localhost:8081/api/auth/google/callback', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ code })
            })
                .then(res => {
                    if (!res.ok) throw new Error('Failed to login with Google');
                    return res.json();
                })
                .then(data => {
                    console.log('User info:', data);
                    setUserInfo(data.result); // giả sử API response là ApiResponse<AuthRespond>
                    // navigate("/dashboard") hoặc set vào localStorage
                })
                .catch((err) => {
                    console.error("❌ Failed to login with Google:", err);

                    if (err.response) {
                        console.log("🔴 Lỗi backend trả về:", err.response.data);
                        console.log("🔴 Status code:", err.response.status);
                    } else if (err.request) {
                        console.log("⚠️ Không nhận được phản hồi từ backend:", err.request);
                    } else {
                        console.log("⚠️ Lỗi khi tạo request:", err.message);
                    }
                });
                  
        } else {
            setError('Code not found in URL');
        }
    }, [location]);

    if (error) return <p style={ { color: 'red' } }>{ error }</p>;
    if (!userInfo) return <p>Đang đăng nhập...</p>;

    return (
        <div style={ { textAlign: 'center', marginTop: '50px' } }>
            <h2>Xin chào, { userInfo.account?.fullName || userInfo.name }!</h2>
            <img
                src={ userInfo.account?.avatar || userInfo.picture }
                alt="Avatar"
                style={ { borderRadius: '50%', width: '120px', height: '120px' } }
            />
            <p>Email: { userInfo.account?.email || userInfo.email }</p>
        </div>
    );
};

export default GoogleCallbackPage;
