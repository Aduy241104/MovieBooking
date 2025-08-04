import axiosInstance from './AxiosConfiguration/axiosInstance'

const viewPersonalProfileAPI = async () => {
    try {
        const response = await axiosInstance.get('/me/profile');
        return response.data;
    } catch (error) {
        throw error;
    }
}

const updateProfileAPI = async (data) => {
    try {
        const response = await axiosInstance.put('/me/update-profile', data);
        return response.data;
    } catch (error) {
        throw error;
    }
}

const changePasswordAPI = async (data) => {
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
        throw new Error("Không thể kết nối đến máy chủ");
    }
};


const requestChangeEmail = async (data) => {
    try {
        const response = await axiosInstance.post("/me/request-change-email", data);
        return response.data;

    } catch (error) {
        if (error.response) {
            if (error.response.data.status === 409 && error.response.data.error === "EMAIL_ALREADY_EXISTS") {
                throw new Error("Email này đã được đăng kí")
            }
        }
        throw new Error("lỗi không xác định vui lòng thử lại sau");
    }
}


const confirmChangeEmailAPI = async (data) => {
    try {
        const response = await axiosInstance.put("/me/confirm-change-email", data);
        return response.data

    } catch (error) {

        if (error.response) {
            throw new Error(error.response.data.message);
        }
        throw new Error("Không thể kết nối đến máy chủ");
    }
}

const updateAvatarAPI = async (data) => {
    try {
        const response = await axiosInstance.put("/me/change-avatar", data);
        return response.data ;

    } catch (error) {
        if (error.response) {
            throw new Error(error.response.data.message);
        }
        throw new Error("Không thể kết nối đến máy chủ");
    }
}

export {
    viewPersonalProfileAPI,
    updateProfileAPI,
    changePasswordAPI,
    requestChangeEmail,
    confirmChangeEmailAPI,
    updateAvatarAPI
};