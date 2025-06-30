import axios from "axios";

const API_URL = "http://localhost:8081/api";

const axiosInstance = axios.create({
    baseURL: API_URL, // Base URL chung cho API
    headers: {
        "Content-Type": "application/json",
    },
});

export const getOtpAPI = async (registerData) => {
    try {
        const response = await fetch("http://localhost:8081/api/auth/register-2", {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(registerData)
        });

        if (response.status === 409) {
            return {
                success: false,
                message: "Email đã tồn tại. Vui lòng chọn tài khoản khác."
            }
        }
        return { success: true };

    } catch (error) {
        console.error("Lỗi kết nối đến server");
        return {
            success: false,
            message: "Không thế kết nối máy chủ"
        }
    }
}

export const veiryfyOtpAPI = async (data) => {
    try {
        const response = await fetch("http://localhost:8081/api/auth/verify-otp", {
            method: "POST",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (response.status === 401) {
            return {
                success: false,
                message: response.message
            }
        }
        return {
            success: true
        }

    } catch (error) {
        console.error("Lỗi kết nối đến server");
        return {
            success: false,
            message: "Không thế kết nối máy chủ"
        }
    }
}

export const loginOAuth = async (loginData) => {
    try {
        const response = await axiosInstance.post("/auth/login-oauth", loginData);
        return response.data; // Thành công: trả dữ liệu
    } catch (error) {
        // Trường hợp server trả về lỗi HTTP như 401, 400...
        if (error.response) {
            throw new Error("Tài khoản hoặc mật khẩu không đúng");
        }

        // Trường hợp lỗi mạng, không phản hồi từ server
        if (error.request) {
            throw new Error("Không thể kết nối đến máy chủ.");
        }

        // Trường hợp lỗi khác
        throw new Error("Đã xảy ra lỗi không xác định.");
    }
};


export const requestForgotPasswordAPI = async (data) => {
    try {
        const response = await axiosInstance.post('/auth/forgot-password', data)
        return { success: true, data: response.data };

    } catch (error) {
        if (error.response) {
            return {
                success: false,
                status: error.response.status,
                message: error.response.message
            }
        }
        return {
            success: false,
            status: 500,
            message: "Không thể kết nối đến máy chủ"
        }
    }
}

export const resetPasswordAPI = async (data) => {
    try {
        const response = await axiosInstance.post("/auth/reset-password", data);
        return { success: true, data: response.data }

    } catch (error) {
        if (error.response) {
            return {
                success: false,
                status: error.response.status,
                message: error.response.message
            }
        }
        return {
            success: false,
            status: 500,
            message: "Không thể kết nối đến máy chủ"
        }
    }
}
