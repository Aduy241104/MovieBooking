import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

const axiosInstance = axios.create({
    baseURL: API_BASE_URL,
});

axiosInstance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

/*
// Optional: Interceptor for response handling (e.g., redirect on 401)
axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            // Nên dùng history của react-router-dom để redirect thay vì window.location
            // history.push('/login'); 
            // Hoặc dispatch một action trong AuthContext để xử lý logout
            console.error("Unauthorized, redirecting to login.");
            // window.location.href = '/login'; // Tạm thời
        }
        return Promise.reject(error);
    }
);
*/

export default axiosInstance;