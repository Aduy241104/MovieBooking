import axios from "axios";

const API_URL = process.env.REACT_APP_BASE_URL;


const axiosInstance = axios.create({
    baseURL: API_URL,
    headers: {
        "Content-Type": "application/json",
    }
});


// Hàm lấy token từ localStorage
export function getAuthHeaders() {
    const token = localStorage.getItem("token");
    return token ? { Authorization: `Bearer ${token}` } : {};
}

// Thêm Interceptor cho request
axiosInstance.interceptors.request.use(
    (config) => {
        const headers = getAuthHeaders();
        if (headers.Authorization) {
            config.headers.Authorization = headers.Authorization;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default axiosInstance;
