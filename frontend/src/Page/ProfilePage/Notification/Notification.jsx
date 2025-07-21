import { useEffect, useState, useContext, useRef } from "react";
import { Trash2, CheckCircle2 } from "lucide-react";
import dayjs from "dayjs";
import axios from "../../../config/axios";
import { deleteNotificationAPI } from "../../../service/NotificationService";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { AuthContext } from "../../../context/AuthContext";
import { message, Popconfirm } from "antd";

export const Notification = () => {
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(false);
    const [timePreset, setTimePreset] = useState("all");
    const [page, setPage] = useState(1);
    const pageSize = 7;

    const { user } = useContext(AuthContext);
    const stompClientRef = useRef(null);

    // Preset filter
    const timePresets = [
        { label: "Tất cả", value: "all" },
        { label: "Hôm nay", value: "today" },
        { label: "3 ngày trước", value: "3d" },
        { label: "7 ngày trước", value: "7d" },
        { label: "Tháng này", value: "month" },
    ];

    // Fetch notifications
    const fetchNotifications = async () => {
        setLoading(true);
        try {
            const res = await axios.get("/notifications");
            setNotifications(Array.isArray(res) ? res : []);
        } catch {
            setNotifications([]);
        } finally {
            setLoading(false);
        }
    };
    useEffect(() => {
        fetchNotifications();
    }, []);

    // WebSocket realtime notification
    useEffect(() => {
        if (!user || !user.accountID) return;
        const client = new Client({
            webSocketFactory: () => new SockJS("http://localhost:8081/ws-notification"),
            reconnectDelay: 15000,
            onConnect: () => {
                client.subscribe(`/queue/notify-${user.accountID}`, (msg) => {
                    const notification = JSON.parse(msg.body);
                    setNotifications((prev) => [notification, ...prev]);
                });
            },
        });
        client.activate();
        stompClientRef.current = client;
        return () => {
            client.deactivate();
        };
    }, [user]);

    // Filter logic
    const filterNotifications = () => {
        let filtered = [...notifications];
        const now = dayjs();
        if (timePreset !== "all") {
            filtered = filtered.filter((n) => {
                const created = n.createdAt ? dayjs(n.createdAt) : null;
                if (!created) return false;
                switch (timePreset) {
                    case "today":
                        return created.isSame(now, "day");
                    case "3d":
                        return now.diff(created, "day") < 3;
                    case "7d":
                        return now.diff(created, "day") < 7;
                    case "month":
                        return created.isSame(now, "month");
                    default:
                        return true;
                }
            });
        }
        return filtered;
    };

    // Reset page when filter changes
    useEffect(() => {
        setPage(1);
    }, [timePreset]);

    // Delete one notification
    const handleDeleteNotification = async (id) => {
        try {
            await deleteNotificationAPI(id);
            setNotifications((prev) => prev.filter((n) => n.id !== id));
            message.success("Xoá thông báo thành công");
        } catch (error) {
            message.error(error?.response?.data?.message || error.message || "Lỗi không xác định");
        }
    };

    // Delete all notifications in filter
    const handleDeleteAllNotifications = async () => {
        try {
            let from = null;
            let to = null;
            const now = dayjs();
            switch (timePreset) {
                case "today":
                    from = now.startOf("day").toISOString();
                    to = now.endOf("day").toISOString();
                    break;
                case "3d":
                    from = now.subtract(3, "day").startOf("day").toISOString();
                    to = now.endOf("day").toISOString();
                    break;
                case "7d":
                    from = now.subtract(7, "day").startOf("day").toISOString();
                    to = now.endOf("day").toISOString();
                    break;
                case "month":
                    from = now.startOf("month").toISOString();
                    to = now.endOf("month").toISOString();
                    break;
                default:
                    from = null;
                    to = null;
            }
            let url = "/notifications/range";
            const params = [];
            if (from) params.push(`from=${encodeURIComponent(from)}`);
            if (to) params.push(`to=${encodeURIComponent(to)}`);
            if (params.length > 0) url += `?${params.join("&")}`;
            await axios.delete(url);
            setNotifications((prev) =>
                prev.filter((n) => {
                    const created = n.createdAt ? dayjs(n.createdAt) : null;
                    if (!created) return true;
                    switch (timePreset) {
                        case "today":
                            return !created.isSame(now, "day");
                        case "3d":
                            return !(now.diff(created, "day") < 3);
                        case "7d":
                            return !(now.diff(created, "day") < 7);
                        case "month":
                            return !created.isSame(now, "month");
                        default:
                            return false;
                    }
                })
            );
            message.success("Đã xoá tất cả thông báo");
        } catch (error) {
            message.error(error?.response?.data?.message || error.message || "Lỗi không xác định");
        }
    };

    // Đánh dấu đã đọc 1 thông báo
    const handleMarkAsRead = async (id) => {
        try {
            await axios.post(`/notifications/${id}/read`);
            setNotifications((prev) => {
                const newList = prev.map((n) => (n.id === id ? { ...n, isRead: true } : n));
                return newList;
            });
        } catch {}
    };
    // Đánh dấu tất cả đã đọc
    const handleMarkAllAsRead = async () => {
        const unread = notifications.filter((n) => !n.isRead);
        for (const n of unread) {
            try {
                await axios.post(`/notifications/${n.id}/read`);
            } catch {}
        }
        setNotifications((prev) => {
            const newList = prev.map((n) => ({ ...n, isRead: true }));
            return newList;
        });
    };

    const filtered = filterNotifications();
    const paged = filtered.slice(0, page * pageSize);

    return (
        <div className="min-h-screen text-white">
            {/* Header Section */}
            <div className="border-b border-[#394264] sticky top-0 z-10 bg-[#181c23]/95">
                <div className="pb-3 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <p className="text-[32px] font-semibold tracking-tight">Thông báo</p>
                    </div>
                    <span className="text-base text-gray-400">
                        Tổng: <span className="text-blue-400 font-bold">{notifications.length}</span>
                    </span>
                </div>
            </div>

            {/* Main Content */}
            <div className="!rounded-xl mx-auto mt-4 px-4 py-8 bg-[#24283b]">
                {/* Filter Section */}
                <div className="max-w-2xl mx-auto rounded-lg mt-2 mb-9">
                    <div className="flex justify-center flex-wrap gap-2">
                        {timePresets.map((t) => (
                            <button
                                key={t.value}
                                className={`px-3 py-1 rounded border !border-gray-600/80 !text-sm font-medium transition-colors duration-150 focus:outline-none ${
                                    timePreset === t.value
                                        ? "bg-blue-500 text-white"
                                        : "bg-[#23272f] text-gray-200 border-[#23272f] hover:bg-blue-900/20"
                                }`}
                                onClick={() => setTimePreset(t.value)}
                            >
                                {t.label}
                            </button>
                        ))}
                    </div>

                    {filtered.length > 0 && (
                        <div className="flex justify-end mt-4 gap-3">
                            {/* Nút đánh dấu tất cả đã đọc */}
                            {notifications.some((n) => !n.isRead) && (
                                <button
                                    className="ml-4 flex items-center gap-1 px-2 py-1 rounded bg-transparent text-green-400 hover:!bg-green-900/40 text-xs font-medium transition-colors"
                                    onClick={handleMarkAllAsRead}
                                >
                                    <CheckCircle2 size={14} /> Đánh dấu tất cả đã đọc
                                </button>
                            )}
                            {/* Nút xoá tất cả thông báo */}
                            <Popconfirm
                                title="Bạn chắc chắn xoá?"
                                onConfirm={handleDeleteAllNotifications}
                                okText="Xoá"
                                cancelText="Huỷ"
                                placement="topRight"
                            >
                                <button className="flex items-center gap-1 px-2 py-1 rounded bg-transparent text-red-400 hover:!bg-gray-700/60 text-xs font-medium transition-colors">
                                    <Trash2 size={14} /> Xoá tất cả
                                </button>
                            </Popconfirm>
                        </div>
                    )}
                </div>

                {/* Notification List */}
                <div className="max-w-3xl mx-auto space-y-3">
                    {loading ? (
                        <div className="text-center py-10 text-gray-400 text-sm">Đang tải...</div>
                    ) : paged.length === 0 ? (
                        <div className="text-center py-10 text-gray-500 text-lg">Không có thông báo nào</div>
                    ) : (
                        <>
                            {paged.map((n) => (
                                <div
                                    key={n.id}
                                    className={`flex items-start gap-3 bg-[#20242c] border border-[#23272f] rounded-lg px-4 py-3 text-sm hover:bg-[#23272c]/80 transition-colors cursor-pointer`}
                                    onClick={() => !n.isRead && handleMarkAsRead(n.id)}
                                >
                                    <div
                                        className={`w-2 h-2 rounded-full mt-2 flex-shrink-0 ${
                                            !n.isRead ? "bg-blue-400" : "bg-gray-600"
                                        }`}
                                    />
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-center justify-between">
                                            <span
                                                className={`font-medium ${!n.isRead ? "text-white" : "text-gray-300"}`}
                                            >
                                                {n.title}
                                            </span>
                                            <button
                                                className="p-1 rounded hover:bg-red-900/20 text-red-400 hover:text-red-300 transition-colors"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    handleDeleteNotification(n.id);
                                                }}
                                                title="Xoá thông báo"
                                            >
                                                <Trash2 size={14} />
                                            </button>
                                        </div>
                                        <span className="text-xs text-gray-500 block mt-0.5">
                                            {n.createdAt ? dayjs(n.createdAt).format("DD/MM/YYYY HH:mm") : ""}
                                        </span>
                                        <div className="text-gray-200 mt-1">{n.content}</div>
                                    </div>
                                </div>
                            ))}
                            {filtered.length > page * pageSize && (
                                <div className="text-center pt-4">
                                    <button
                                        className="px-4 py-2 rounded bg-blue-500 text-white hover:bg-blue-600 text-xs font-medium"
                                        onClick={() => setPage((p) => p + 1)}
                                    >
                                        Xem thêm
                                    </button>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};
