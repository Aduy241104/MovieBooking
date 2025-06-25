//locpng
import axiosInstance from '../config/axiosBooking';

const API_URL = '/public/payment-methods';

export const getActivePaymentMethods = () => {
    return axiosInstance.get(API_URL);
};