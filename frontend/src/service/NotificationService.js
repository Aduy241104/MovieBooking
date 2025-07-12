import axios from "../config/axios";

export const fetchNotificationsAPI = () => {
    return axios.get("/notifications");
};

export const fetchNotificationsByTimeAPI = (from, to) => {
    const params = {};
    if (from) params.from = from;
    if (to) params.to = to;
    return axios.get("/notifications/filter", { params });
};

export const markNotificationAsReadAPI = (id) => {
    return axios.post(`/notifications/${id}/read`);
};

export const deleteNotificationAPI = (id) => {
    return axios.delete(`/notifications/${id}`);
};

export const deleteNotificationsBeforeAPI = (before) => {
    return axios.delete(`/notifications/before`, { params: { before } });
};

// API gửi thông báo cho nhiều user
export const sendNotificationAPI = (payload) => {
    return axios.post("/notifications/send", payload);
};

// API gửi thông báo cho tất cả user
export const sendNotificationToAllAPI = (payload) => {
    return axios.post("/notifications/send-all", payload);
};
