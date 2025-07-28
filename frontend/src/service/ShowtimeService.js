import axios from "../config/axios";

// Thêm lịch chiếu
const createShowtimeAPI = (showtimeRequest) => {
    const URL_BACKEND = `/public/movieSchedule/admin/add-time`;
    return axios.post(URL_BACKEND, showtimeRequest);
};

// Sửa lịch chiếu
const updateShowtimeAPI = (id, showtimeRequest) => {
    const URL_BACKEND = `/public/movieSchedule/admin/update-time/${id}`;
    return axios.put(URL_BACKEND, showtimeRequest);
};

// Xóa mềm lịch chiếu
const deleteShowtimeAPI = (id) => {
    const URL_BACKEND = `/public/movieSchedule/admin/delete-time/${id}`;
    return axios.delete(URL_BACKEND);
};

export {
    createShowtimeAPI,
    updateShowtimeAPI,
    deleteShowtimeAPI
};
