
import axiosInstance from '../config/axiosBooking';
const BASE_URL = 'http://localhost:8081/api/public/payment-methods';

export const getActivePaymentMethods = () => {
     return axiosInstance.get(BASE_URL);
};

