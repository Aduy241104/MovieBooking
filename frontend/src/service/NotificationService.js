import axios from "../config/axios";

export const fetchNotificationsAPI = () => {
    return axios.get("/notifications");
};

export const markNotificationAsReadAPI = (id) => {
    return axios.post(`/notifications/${id}/read`);
};