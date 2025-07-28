import axiosInstance from "./AxiosConfiguration/axiosInstance";

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

        if (error.response) {
            throw new Error("Tài khoản hoặc mật khẩu không đúng");
        }

        if (error.request) {
            throw new Error("Không thể kết nối đến máy chủ.");
        }

        throw new Error("Đã xảy ra lỗi không xác định.");
    }
};


export const requestForgotPasswordAPI = async (data) => {
    try {
        const response = await axiosInstance.post('/auth/forgot-password', data);
        return response.data;

    } catch (error) {
        const status = error.response?.status;
        const errorCode = error.response?.data?.error;

        if (status === 404 && errorCode === "RESOURCE_NOT_FOUND") {
            throw new Error("Tài khoản không tồn tại");
        }
        
        throw new Error("Đã có lỗi xảy ra, vui lòng thử lại");
    }
};


export const resetPasswordAPI = async (data) => {
    try {
        const response = await axiosInstance.post("/auth/reset-password", data);
        return { success: true, data: response.data }

    } catch (error) {

        if (error.response) {
            if (error.response && error.response.data.status === 401) {
                throw new Error("OTP không hợp lệ");
            } else if (error.response.data.status === 404 && error.response.data.error === "RESOURCE_NOT_FOUND") {
                throw new Error("Không thể tìm thấy tài khoản vui lòng thử lại")
            }
        }
        throw new Error("Không thể kết nối đến máy chủ");
    }
}

export const handleLoginGoogleApi = async (idToken) => {
    try {
        const response = await axiosInstance.post("/auth/google-login", { idToken: idToken });
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
