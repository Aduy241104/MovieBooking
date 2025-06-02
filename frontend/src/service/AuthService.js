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