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


export const viewPersonalProfileAPI = async () => {
    try {
        const response = await axiosInstance.get('/me/profile');
        return response.data;
    } catch (error) {
        throw error;
    }
}

export const updateProfileAPI = async (data) => {
    try {
        const response = await axiosInstance.put('/me/update-profile', data);

        return response;
    } catch (error) {
        throw error;
    }
}

export const changePasswordAPI = async (data) => {
    try {
        const response = await axiosInstance.put('/me/change-password', data);
        return { success: true, data: response.data };
    } catch (error) {
        if (error.response) {
            return {
                success: false,
                status: error.response.status,
                message: error.response.data.message || "Có lỗi xảy ra"
            };
        }
        // Trường hợp lỗi không phải từ server (mất mạng, timeout,...)
        return {
            success: false,
            status: 500,
            message: "Không thể kết nối đến máy chủ"
        };
    }
};


export const requestChangeEmail = async (data) => {
    try {
        const response = await axiosInstance.post("/me/request-change-email", data);
        return { success: true, data: response.data }

    } catch (error) {
        if (error.response) {
            return {
                success: false,
                status: error.response.status,
                message: error.response.data.message || "Có lỗi xảy ra"
            };
        }
        return {
            success: false,
            status: 500,
            message: "Không thể kết nối đến máy chủ"
        };
    }
}


export const confirmChangeEmailAPI = async (data) => {
    try {
        const response = await axiosInstance.put("/me/confirm-change-email", data);
        return { success: true, data: response.data }

    } catch (error) {
        console.log(error);

        if (error.response) {
            return {
                success: false,
                status: error.response.status,
                message: error.response.data.message || "Có lỗi xảy ra"
            };
        }
        return {
            success: false,
            status: 500,
            message: "Không thể kết nối đến máy chủ"
        };
    }
}