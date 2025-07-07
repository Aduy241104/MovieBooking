import React from 'react';

const GoogleLoginButtonss = () => {
    const handleGoogleLogin = () => {
        window.location.href = 'http://localhost:8081/api/auth/google/google-login';
    };

    return (
        <div style={ { textAlign: 'center', marginTop: '100px' } }>
            <h2>Đăng nhập bằng Google</h2>
            <button
                onClick={ handleGoogleLogin }
                style={ {
                    padding: '12px 24px',
                    backgroundColor: '#4285F4',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '16px',
                } }
            >
                Đăng nhập với Google
            </button>
        </div>
    );
};

export default GoogleLoginButtonss;
