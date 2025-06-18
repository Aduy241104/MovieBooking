import axios from "../config/axios";


export const fetchActivityLogsAPI = (page, size, filter) => {
    return axios.get(`/admin/activity-logs?page=${page}&size=${size}&filter=${filter}&sort=createdAt,desc`);
};