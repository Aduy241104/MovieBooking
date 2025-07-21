import axios from "../config/axios";

// Lấy toàn bộ lịch chiếu đang hoạt động
const fetchAllActiveShowtimesAPI = () => {
  return axios.get("/movieSchedule/admin/list-isdelete");
};

// Thêm lịch chiếu
const createShowtimeAPI = (showtimeRequest) => {
  return axios.post("/movieSchedule/admin/add-time", showtimeRequest);
};

// Sửa lịch chiếu
const updateShowtimeAPI = (id, showtimeRequest) => {
  return axios.put(`/movieSchedule/admin/update-time/${id}`, showtimeRequest);
};

// Xóa mềm lịch chiếu
const deleteShowtimeAPI = (id) => {
  return axios.delete(`/movieSchedule/admin/delete-time/${id}`);
};

export {
  fetchAllActiveShowtimesAPI,
  createShowtimeAPI,
  updateShowtimeAPI,
  deleteShowtimeAPI,
};
