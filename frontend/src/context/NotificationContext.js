import { createContext, useContext, useState, useCallback } from "react";
import {
    deleteNotificationAPI,
    deleteNotificationsByFilterAPI,
    fetchNotificationsAPI,
    markNotificationAsReadAPI,
} from "../service/NotificationService";
import { message } from "antd";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

const NotificationContext = createContext();

export const useNotification = () => {
    const context = useContext(NotificationContext);
    if (!context) {
        throw new Error("useNotification must be used within a NotificationProvider");
    }
    return context;
};

export const NotificationProvider = ({ children }) => {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [isLoading, setIsLoading] = useState(false);

    // Fetch notifications and update unread count
    const fetchNotifications = useCallback(async () => {
        const token = localStorage.getItem("token");
        if (!token) return;
        try {
            setIsLoading(true);
            const res = await fetchNotificationsAPI();
            setNotifications(res);
            const newUnreadCount = res.filter((n) => !n.isRead).length;
            setUnreadCount(newUnreadCount);
            return res;
        } catch (err) {
            if (err.message === "Network Error") {
                console.error("Network error while fetching notifications:", err);
            } else {
                console.error("Error fetching notifications:", err);
                message.error("Đã có lỗi xảy ra khi tải thông báo.");
            }
            // throw err;
        } finally {
            setIsLoading(false);
        }
    }, []);

    // Mark notification as read
    const markAsRead = async (id) => {
        try {
            await markNotificationAsReadAPI(id);
            setNotifications((prev) => {
                const newList = prev.map((n) => (n.id === id ? { ...n, isRead: true } : n));
                setUnreadCount(newList.filter((n) => !n.isRead).length);
                return newList;
            });
        } catch (error) {
            message.error("Không thể đánh dấu thông báo đã đọc");
        }
    };

    // Mark all notifications as read
    const markAllAsRead = async () => {
        try {
            const unreadNotifications = notifications.filter((n) => !n.isRead);
            for (const notification of unreadNotifications) {
                await markNotificationAsReadAPI(notification.id);
            }
            setNotifications((prev) => {
                const newList = prev.map((n) => ({ ...n, isRead: true }));
                setUnreadCount(0);
                return newList;
            });
        } catch (error) {
            message.error("Không thể đánh dấu tất cả thông báo đã đọc");
        }
    };

    // Add new notification (for WebSocket)
    const addNotification = useCallback((notification) => {
        setNotifications((prev) => {
            // Nếu đã có notification này (id trùng), không thêm nữa
            if (prev.some((n) => n.id === notification.id)) return prev;
            const newList = [notification, ...prev];
            setUnreadCount(newList.filter((n) => !n.isRead).length);
            return newList;
        });
    }, []);

    // THÊM HÀM XÓA 1 THÔNG BÁO
    const deleteNotification = async (id) => {
        try {
            await deleteNotificationAPI(id);
            setNotifications((prev) => {
                const newList = prev.filter((n) => n.id !== id);
                // Cập nhật lại số lượng chưa đọc
                setUnreadCount(newList.filter((n) => !n.isRead).length);
                return newList;
            });
            message.success("Xóa thông báo thành công");
        } catch (error) {
            message.error("Xóa thông báo thất bại");
        }
    };

    // THÊM HÀM XÓA NHIỀU THÔNG BÁO THEO BỘ LỌC
    const deleteNotificationsByFilter = async (filterParams) => {
        try {
            await deleteNotificationsByFilterAPI(filterParams);
            await fetchNotifications();
            message.success("Đã xóa các thông báo đã chọn");
        } catch (error) {
            message.error("Xóa thông báo thất bại");
        }
    };

    // Setup WebSocket connection
    const setupWebSocket = useCallback(
        (accountId) => {
            if (!accountId) return null;

            try {
                const client = new Client({
                    webSocketFactory: () => new SockJS("http://localhost:8081/ws-notification"),
                    reconnectDelay: 15000,
                    onStompError: (frame) => {
                        console.error("STOMP error:", frame);
                    },
                    onWebSocketError: (event) => {
                        console.error("WebSocket error:", event);
                    },
                });

                client.onConnect = () => {
                    client.subscribe(`/queue/notify-${accountId}`, (msg) => {
                        const notification = JSON.parse(msg.body);
                        addNotification(notification);
                    });
                };

                client.activate();
                return client;
            } catch (error) {
                console.error("Error connecting to WebSocket:", error);
                message.error("Không thể kết nối đến máy chủ thông báo. Vui lòng thử lại sau.");
                return null;
            }
        },
        [addNotification]
    );

    const value = {
        notifications,
        unreadCount,
        isLoading,
        fetchNotifications,
        markAsRead,
        markAllAsRead,
        addNotification,
        setupWebSocket,
        setNotifications,
        setUnreadCount,
        deleteNotification,
        deleteNotificationsByFilter,
    };

    return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>;
};
