import axios from "../config/axios";

// Lấy toàn bộ lịch chiếu đang hoạt động
const fetchAllActiveShowtimesAPI = () => {
  return axios.get("/admin/movieSchedule/list-isdelete");
};

// Thêm lịch chiếu
const createShowtimeAPI = (showtimeRequest) => {
  return axios.post("/admin/movieSchedule/add-time", showtimeRequest);
};

// Sửa lịch chiếu
const updateShowtimeAPI = (id, showtimeRequest) => {
  return axios.put(`/admin/movieSchedule/update-time/${id}`, showtimeRequest);
};

// Xóa mềm lịch chiếu
const deleteShowtimeAPI = (id) => {
  return axios.delete(`/admin/movieSchedule/delete-time/${id}`);
};

export {
  fetchAllActiveShowtimesAPI,
  createShowtimeAPI,
  updateShowtimeAPI,
  deleteShowtimeAPI,
};
