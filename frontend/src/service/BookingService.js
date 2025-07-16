//locpng
import axiosInstance from '../config/axiosBooking';
const API_URL = '/bookings'; // Endpoint cần xác thực

export const createBooking = (bookingData) => {
    return axiosInstance.post(API_URL, bookingData);
};

export const checkPromotion = (code) => {
    return axiosInstance.get(`${API_URL}/promotions/check/${code}`);
};

export const getUserBookingHistory = () => {
    return axiosInstance.get(`${API_URL}/history`);
};

export const getBookingDetails = (bookingId) => {
    return axiosInstance.get(`${API_URL}/${bookingId}/details`);
}
export const getUserPoints = () => {
    return axiosInstance.get(`${API_URL}/points`);
};
// <<< THÊM HÀM MỚI NÀY >>>
export const retryPayment = (bookingId) => {
    return axiosInstance.post(`/bookings/${bookingId}/retry-payment`);
};
// Hàm vnpayReturn không cần gọi từ frontend, backend sẽ tự redirect