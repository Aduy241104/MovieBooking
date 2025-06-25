
// src/service/PaymentMethodService.js
import axios from 'axios';
const BASE_URL = 'http://localhost:8081/api/admin/payment-methods'; 

const instance = axios.create({
    baseURL: BASE_URL
});

instance.interceptors.request.use(function (config){
    const token = localStorage.getItem("token");
    if(token){
        config.headers.Authorization = `Bearer ${token}` 
    }
    return config; 

}, function (error){
    return Promise.reject(error)
})

// named export
export const fetchAllPaymentMethodAPI = () => instance.get(BASE_URL);
export const addPaymentMethodAPI = data => axios.post(BASE_URL, data);
export const updatePaymentMethodAPI = (id, data) => axios.put(`${BASE_URL}/${id}`, data);
export const togglePaymentMethodActiveAPI = id => axios.put(`${BASE_URL}/${id}/toggle`);
export const deletePaymentMethodAPI = id => axios.delete(`${BASE_URL}/${id}`);


const getAuthHeader = () => {
    const tokenStr = localStorage.getItem('token');
    // console.log(localStorage.getItem("token"));
    let token = tokenStr;

    try{
        const obj = JSON.parse(tokenStr);
        if (obj.token) token = obj.token;
    } catch(_) {}

    if (!token) return{};

    return {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
    };
};


const PaymentMethodService = {
    // Lấy tất cả phương thức thanh toán


    async fetchAll() {
        const res = await fetch(BASE_URL, {
            method: 'GET',
        
            headers: getAuthHeader()
        });
        if (!res.ok) throw new Error("Không thể lấy danh sách phương thức thanh toán");
        return res.json();
    },

    // Thêm mới phương thức thanh toán
    async add(data) {
        const res = await fetch(`${BASE_URL}`, {
            method: 'POST',
            body: JSON.stringify(data),
        });
        if (!res.ok) {
            const error = await res.text();
            throw new Error(error || "Thêm phương thức thất bại");
        }
        return res.json();
    },

    // Cập nhật phương thức thanh toán theo ID
    async update(id, data) {
        const res = await fetch(`${BASE_URL}/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data),
        });
        if (!res.ok) throw new Error("Cập nhật phương thức thất bại");
        return res.json();
    },

    // Bật/tắt trạng thái hoạt động
    async toggleActive(id) {
        const res = await fetch(`${BASE_URL}/${id}/toggle`, {
            method: 'PUT'
        });
        if (!res.ok) throw new Error("Thay đổi trạng thái thất bại");
        return res.json();
    },

    // Xóa phương thức thanh toán
    async delete(id) {
        const res = await fetch(`${BASE_URL}/${id}`, {
            method: 'DELETE'
        });
        if (!res.ok) throw new Error("Xóa phương thức thất bại");
        return res;
    }
};

export default PaymentMethodService;
