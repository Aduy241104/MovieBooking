import { DeleteOutlined } from "@ant-design/icons";
import { Button, message, Modal, Tooltip } from "antd";
import { Bell } from "lucide-react";
import axios from "../../config/axios";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { format } from "date-fns";
import { deleteNotificationAPI } from "../../service/NotificationService";
import dayjs from "dayjs";
import { useContext, useEffect, useRef, useState } from "react";
import { AuthContext } from "../../context/AuthContext";

// NotificationBell component
export function NotificationBell() {
    const { user } = useContext(AuthContext);
    const [notifOpen, setNotifOpen] = useState(false);
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showAllModal, setShowAllModal] = useState(false);
    const [modalTimePreset, setModalTimePreset] = useState("all");
    const [modalPage, setModalPage] = useState(1);
    const modalPageSize = 5;
    const bellRef = useRef(null);
    const unreadCount = Array.isArray(notifications) ? notifications.filter((n) => !n.isRead).length : 0;

    // Định nghĩa timePresets
    const timePresets = [
        { label: "Tất cả", value: "all" },
        { label: "Hôm nay", value: "today" },
        { label: "3 ngày trước", value: "3d" },
        { label: "7 ngày trước", value: "7d" },
        { label: "Tháng này", value: "month" },
    ];

    // Lấy notification từ backend khi mount hoặc mở popup
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
        if (notifOpen) fetchNotifications();
    }, [notifOpen]);
    useEffect(() => {
        fetchNotifications();
    }, []);

    // WebSocket realtime notification
    useEffect(() => {
        if (!user || !user.accountID) return;
        let client = new Client({
            webSocketFactory: () => new SockJS("http://localhost:8081/ws-notification"),
            reconnectDelay: 15000,
            onConnect: () => {
                console.log("WebSocket connected for user:", user.accountID);
                client.subscribe(`/queue/notify-${user.accountID}`, (msg) => {
                    console.log("New notification received:", msg.body);
                    const notification = JSON.parse(msg.body);
                    setNotifications((prev) => [notification, ...prev]);
                });
            },
            onDisconnect: () => {
                console.log("WebSocket disconnected");
            },
            onStompError: (frame) => {
                console.error("WebSocket error:", frame);
            },
        });
        client.activate();
        return () => {
            client.deactivate();
        };
    }, [user]);

    // Đóng popup khi click ra ngoài, nhưng KHÔNG khi modal tất cả thông báo đang mở
    useEffect(() => {
        function handleClickOutside(event) {
            if (bellRef.current && !bellRef.current.contains(event.target)) {
                setNotifOpen(false);
            }
        }
        if (notifOpen && !showAllModal) {
            document.addEventListener("mousedown", handleClickOutside);
        } else {
            document.removeEventListener("mousedown", handleClickOutside);
        }
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [notifOpen, showAllModal]);

    // Đánh dấu đã đọc khi click notification
    const handleMarkAsRead = async (id) => {
        try {
            await axios.post(`/notifications/${id}/read`);
            setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)));
        } catch {}
    };
    // Đánh dấu tất cả đã đọc (nếu muốn dùng)
    const handleMarkAllAsRead = async () => {
        const unread = notifications.filter((n) => !n.isRead);
        for (const n of unread) {
            try {
                await axios.post(`/notifications/${n.id}/read`);
            } catch {}
        }
        setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    };

    // Xoá 1 thông báo
    const handleDeleteNotification = async (id) => {
        try {
            await deleteNotificationAPI(id);
            setNotifications((prev) => prev.filter((n) => n.id !== id));
            message.success("Xoá thông báo thành công");
        } catch (error) {
            message.error(
                `Xoá thông báo thất bại: ${error?.response?.data?.message || error.message || "Lỗi không xác định"}`
            );
        }
    };

    // Xoá tất cả thông báo
    const handleDeleteAllNotifications = async () => {
        try {
            // Xác định khoảng thời gian filter
            let from = null;
            let to = null;
            const now = dayjs();
            switch (modalTimePreset) {
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
            // Sau khi xoá, chỉ xoá các thông báo thuộc filter khỏi state
            setNotifications((prev) =>
                prev.filter((n) => {
                    const created = n.createdAt ? dayjs(n.createdAt) : null;
                    if (!created) return true; // giữ lại nếu không xác định được ngày
                    switch (modalTimePreset) {
                        case "today":
                            return !created.isSame(now, "day");
                        case "3d":
                            return !(now.diff(created, "day") < 3);
                        case "7d":
                            return !(now.diff(created, "day") < 7);
                        case "month":
                            return !created.isSame(now, "month");
                        default:
                            return false; // nếu là 'all' thì xoá hết
                    }
                })
            );
            message.success("Đã xoá tất cả thông báo");
        } catch (error) {
            message.error(
                `Xoá tất cả thông báo thất bại: ${
                    error?.response?.data?.message || error.message || "Lỗi không xác định"
                }`
            );
        }
    };

    // Lọc trong modal theo preset
    const filterModalNotifications = () => {
        let filtered = [...notifications];
        const now = dayjs();
        if (modalTimePreset !== "all") {
            filtered = filtered.filter((n) => {
                const created = n.createdAt ? dayjs(n.createdAt) : null;
                if (!created) return false;
                switch (modalTimePreset) {
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

    // Khi đổi filter thì reset page về 1
    useEffect(() => {
        setModalPage(1);
    }, [modalTimePreset]);

    return (
        <div className="relative mr-3" ref={bellRef}>
            <button
                className="relative p-2 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-150 flex items-center justify-center"
                onClick={() => setNotifOpen((v) => !v)}
                aria-label="Thông báo"
                style={{ borderRadius: "50%" }}
            >
                <Bell
                    size={20}
                    strokeWidth={2}
                    className="text-gray-600 hover:text-blue-600 transition-colors duration-150"
                />
                {unreadCount > 0 && (
                    <span className="absolute -top-0.5 -right-0.5 bg-gradient-to-br from-red-500 to-red-600 text-white text-xs font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center border-2 border-white shadow-lg animate-pulse">
                        {unreadCount > 9 ? "9+" : unreadCount}
                    </span>
                )}
            </button>

            {/* Popup danh sách notification */}
            {notifOpen && (
                <div className="absolute right-0 mt-3 w-86 bg-white rounded-xl shadow-2xl border border-gray-200 z-50 overflow-hidden transform transition-all duration-200 ease-out animate-in slide-in-from-top-2">
                    {/* Header */}
                    <div className="bg-gradient-to-r from-blue-50 to-indigo-50 px-4 pt-3 pb-2 border-b border-gray-200">
                        <div className="flex flex-wrap items-center justify-between">
                            <div className="flex items-center space-x-2">
                                <Bell size={18} className="text-blue-600" />
                                <p className="text-lg font-semibold text-gray-800">Thông báo</p>
                            </div>
                            <span className="bg-blue-100 text-blue-700 text-xs font-medium px-2.5 py-1 rounded-full">
                                {unreadCount} mới
                            </span>
                            {unreadCount > 0 && (
                                <button
                                    className="text-xs text-blue-600 font-medium transition-colors duration-150 px-2 py-1 hover:bg-sky-200/60"
                                    style={{ height: 24, minHeight: 0, lineHeight: 0, borderRadius: "4px" }}
                                    onClick={handleMarkAllAsRead}
                                >
                                    Đánh dấu tất cả đã đọc
                                </button>
                            )}
                        </div>
                    </div>

                    {/* Danh sách notification */}
                    <div className="max-h-80 overflow-y-auto">
                        {loading ? (
                            <div className="py-8 text-center text-gray-400 text-sm">Đang tải...</div>
                        ) : notifications.length === 0 ? (
                            <div className="py-12 text-center">
                                <Bell size={48} className="mx-auto text-gray-300 mb-4" />
                                <p className="text-gray-500 text-sm">Không có thông báo nào</p>
                            </div>
                        ) : (
                            <div className="divide-y divide-gray-100">
                                {notifications.slice(0, 5).map((n) => (
                                    <div
                                        key={n.id}
                                        className={`px-3 py-2 hover:bg-gray-50 transition-colors duration-150 cursor-pointer ${
                                            !n.isRead ? "bg-blue-50/50 border-l-4 border-blue-400" : ""
                                        }`}
                                    >
                                        <div className="flex items-start space-x-3">
                                            <div
                                                className={`w-2 h-2 rounded-full mt-2 flex-shrink-0 ${
                                                    !n.isRead ? "bg-blue-500" : "bg-gray-300"
                                                }`}
                                            />
                                            <div
                                                className="flex-1 min-w-0"
                                                onClick={() => !n.isRead && handleMarkAsRead(n.id)}
                                            >
                                                <p
                                                    className={`text-sm leading-5 ${
                                                        !n.isRead ? "text-gray-900 font-medium" : "text-gray-700"
                                                    }`}
                                                >
                                                    {n.title}
                                                </p>
                                                <p className="text-xs text-gray-500 mt-1">
                                                    {n.createdAt
                                                        ? format(new Date(n.createdAt), "dd/MM/yyyy HH:mm a")
                                                        : ""}
                                                </p>
                                                <p className="text-sm text-gray-700 mt-1">{n.content}</p>
                                            </div>
                                            <Tooltip title="Xoá thông báo">
                                                <Button
                                                    onClick={() => handleDeleteNotification(n.id)}
                                                    type="text"
                                                    size="small"
                                                    icon={<DeleteOutlined />}
                                                    danger
                                                />
                                            </Tooltip>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    {/* Footer */}
                    <div
                        className="flex bg-gray-50 border-t border-gray-200"
                        style={{ height: "56px", minHeight: 0, lineHeight: 0 }}
                    >
                        <button
                            className="w-full text-sm text-blue-600 hover:text-blue-700 hover:bg-cyan-200/15 font-medium transition-colors duration-150"
                            onClick={() => setShowAllModal(true)}
                        >
                            Xem tất cả thông báo
                        </button>
                    </div>
                </div>
            )}

            {/* Modal xem tất cả thông báo */}
            <Modal
                open={showAllModal}
                onCancel={(e) => {
                    e?.stopPropagation?.();
                    setShowAllModal(false);
                }}
                footer={null}
                title="Tất cả thông báo"
                width={600}
                style={{ padding: 0 }}
                centered
            >
                <div className="p-2">
                    {/* Bộ lọc preset thời gian */}
                    <div className="flex flex-wrap gap-2">
                        {timePresets.map((t) => (
                            <button
                                key={t.value}
                                className={`px-3 py-1 !rounded-full border text-xs font-medium transition-colors duration-150 ${
                                    modalTimePreset === t.value
                                        ? "bg-blue-500 text-white border-blue-500"
                                        : "bg-white text-gray-700 border-gray-300 hover:bg-blue-50"
                                }`}
                                onClick={() => setModalTimePreset(t.value)}
                            >
                                {t.label}
                            </button>
                        ))}
                    </div>
                    {/* Nút xoá tất cả */}
                    {notifications.length > 0 && (
                        <div className="mt-4 mb-3 flex justify-end">
                            <Button danger size="small" onClick={handleDeleteAllNotifications}>
                                Xoá tất cả
                            </Button>
                        </div>
                    )}
                    {/* Danh sách */}
                    <div className="divide-y divide-gray-100 max-h-[500px] overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-gray-100">
                        {filterModalNotifications().length === 0 ? (
                            <div className="py-12 text-center">
                                <Bell size={48} className="mx-auto text-gray-300 mb-4" />
                                <p className="text-gray-500 text-sm">Không có thông báo nào</p>
                            </div>
                        ) : (
                            <>
                                {filterModalNotifications()
                                    .slice(0, modalPage * modalPageSize)
                                    .map((n) => (
                                        <div
                                            key={n.id}
                                            className={`p-3 hover:bg-gray-50 transition-colors duration-150 cursor-pointer ${
                                                !n.isRead ? "bg-blue-50/50 border-l-4 border-blue-400" : ""
                                            }`}
                                            onClick={() => !n.isRead && handleMarkAsRead(n.id)}
                                        >
                                            <div className="flex items-start space-x-3">
                                                <div
                                                    className={`w-2 h-2 rounded-full mt-2 flex-shrink-0 ${
                                                        !n.isRead ? "bg-blue-500" : "bg-gray-300"
                                                    }`}
                                                />
                                                <div className="flex-1 min-w-0">
                                                    <p
                                                        className={`text-sm leading-5 ${
                                                            !n.isRead ? "text-gray-900 font-medium" : "text-gray-700"
                                                        }`}
                                                    >
                                                        {n.title}
                                                    </p>
                                                    <p className="text-xs text-gray-500 mt-1">
                                                        {n.createdAt
                                                            ? format(new Date(n.createdAt), "dd/MM/yyyy HH:mm a")
                                                            : ""}
                                                    </p>
                                                    <p className="text-sm text-gray-700 mt-1">{n.content}</p>
                                                </div>
                                                <Tooltip title="Xoá thông báo">
                                                    <Button
                                                        onClick={() => handleDeleteNotification(n.id)}
                                                        type="text"
                                                        size="small"
                                                        icon={<DeleteOutlined />}
                                                        danger
                                                    />
                                                </Tooltip>
                                            </div>
                                        </div>
                                    ))}
                                {filterModalNotifications().length > modalPage * modalPageSize && (
                                    <div className="pt-4 pb-3 text-center">
                                        <button
                                            className="px-4 py-2 rounded bg-blue-100 text-blue-600 hover:bg-blue-200 text-sm font-medium"
                                            onClick={() => setModalPage((p) => p + 1)}
                                        >
                                            Xem thêm
                                        </button>
                                    </div>
                                )}
                            </>
                        )}
                    </div>
                </div>
            </Modal>
        </div>
    );
}
