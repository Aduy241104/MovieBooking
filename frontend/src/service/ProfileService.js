import axios from "axios";

const API_URL = process.env.REACT_APP_BASE_URL;

const token ="eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJhbmhkdXkuY29tIiwic3ViIjoiNiIsImV4cCI6MTc1MDIxNDk0OSwiaWF0IjoxNzUwMjExMzQ5LCJzY29wZSI6IkNVU1RPTUVSIn0.6doEQA5X2oi4LHcitDPljwSaQRHTUTG8uksfq7AvOt6ypkEdy4fWG5x4GfP8F7hqDCInueZ5S4nut71wIaJ-lg"

const axiosInstance = axios.create({
    baseURL: "http://localhost:8081/api/",
    headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
    }
});



// // Hàm lấy token từ localStorage
// export function getAuthHeaders() {
//     const token = localStorage.getItem("token");
//     return token ? { Authorization: `Bearer ${token}` } : {};
// }

// // Thêm Interceptor cho request
// axiosInstance.interceptors.request.use(
//     (config) => {
//         const headers = getAuthHeaders();
//         if (headers.Authorization) {
//             config.headers.Authorization = headers.Authorization;
//         }
//         return config;
//     },
//     (error) => {
//         return Promise.reject(error);
//     }
// );


export const viewPersonalProfileAPI = async () => {
    try {
        const response = await axiosInstance.get('me/profile');
        console.log("data: ", response);
        return response.data;

    } catch (error) {
        console.log(error);
    }
}