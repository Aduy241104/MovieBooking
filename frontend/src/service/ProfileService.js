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
        // Trường hợp lỗi không phải từ server (mất mạng, timeout,...)
        return {
            success: false,
            status: 500,
            message: "Không thể kết nối đến máy chủ"
        };
    }
};


const requestChangeEmail = async (data) => {
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


const confirmChangeEmailAPI = async (data) => {
    try {
        const response = await axiosInstance.put("/me/confirm-change-email", data);
        console.log(response);

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

const updateAvatarAPI = async (data) => {
    try {
        const response = await axiosInstance.put("/me/change-avatar", data);
        return { success: true, data: response.data };

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

export {
    viewPersonalProfileAPI,
    updateProfileAPI,
    changePasswordAPI,
    requestChangeEmail,
    confirmChangeEmailAPI,
    updateAvatarAPI
};