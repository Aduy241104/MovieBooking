import { Bell } from "lucide-react";
import dayjs from "dayjs";
import { useContext, useEffect, useRef, useState } from "react";
import { AuthContext } from "../../../context/AuthContext";
import { useNotification } from "../../../context/NotificationContext";
import NotificationPopup from "./NotificationPopup";
import AllNotificationsModal from "./AllNotificationsModal";

// NotificationBell component
export default function NotificationBell() {
    const { user } = useContext(AuthContext);
    // Sử dụng state và hàm từ Context
    const { notifications, unreadCount, fetchNotifications, deleteNotificationsByFilter, setupWebSocket } =
        useNotification();

    const [notifyOpen, setNotifyOpen] = useState(false);
    const [showAllModal, setShowAllModal] = useState(false);
    const [modalTimePreset, setModalTimePreset] = useState("all");
    const [modalPage, setModalPage] = useState(1);
    const modalPageSize = 5;
    const bellRef = useRef(null);

    // useEffect để fetch data và thiết lập WebSocket sẽ gọi hàm từ Context
    useEffect(() => {
        // Fetch lần đầu khi component được mount
        fetchNotifications();
    }, [fetchNotifications]);

    useEffect(() => {
        if (!user || !user.accountID) return;
        const client = setupWebSocket(user.accountID);
        return () => {
            if (client) client.deactivate();
        };
    }, [user, setupWebSocket]);

    // Định nghĩa timePresets
    const timePresets = [
        { label: "Tất cả", value: "all" },
        { label: "Hôm nay", value: "today" },
        { label: "3 ngày trước", value: "3d" },
        { label: "7 ngày trước", value: "7d" },
        { label: "Tháng này", value: "month" },
    ];

    // Đóng popup khi click ra ngoài, nhưng KHÔNG khi modal tất cả thông báo đang mở
    useEffect(() => {
        function handleClickOutside(event) {
            if (bellRef.current && !bellRef.current.contains(event.target)) {
                setNotifyOpen(false);
            }
        }
        if (notifyOpen && !showAllModal) {
            document.addEventListener("mousedown", handleClickOutside);
        } else {
            document.removeEventListener("mousedown", handleClickOutside);
        }
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [notifyOpen, showAllModal]);

    // Xoá tất cả thông báo
    const handleDeleteAllNotifications = async () => {
        let params = {};
        const now = dayjs();
        switch (modalTimePreset) {
            case "today":
                params.from = now.startOf("day").toISOString();
                params.to = now.endOf("day").toISOString();
                break;
            case "3d":
                params.from = now.subtract(3, "day").startOf("day").toISOString();
                break;
            case "7d":
                params.from = now.subtract(7, "day").startOf("day").toISOString();
                break;
            case "month":
                params.from = now.startOf("month").toISOString();
                break;
            default: // 'all'
                break;
        }
        // Gọi hàm từ context
        await deleteNotificationsByFilter(params);
    };

    // Lọc trong modal theo preset
    const filterModalNotifications = () => {
        const safeNotifications = Array.isArray(notifications) ? notifications : [];
        let filtered = [...safeNotifications];
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
                onClick={() => setNotifyOpen((v) => !v)}
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
            {notifyOpen && <NotificationPopup setShowAllModal={setShowAllModal} />}

            {/* Modal xem tất cả thông báo */}
            <AllNotificationsModal
                showAllModal={showAllModal}
                setShowAllModal={setShowAllModal}
                modalTimePreset={modalTimePreset}
                setModalTimePreset={setModalTimePreset}
                modalPage={modalPage}
                setModalPage={setModalPage}
                modalPageSize={modalPageSize}
                filterModalNotifications={filterModalNotifications}
                handleDeleteAllNotifications={handleDeleteAllNotifications}
                timePresets={timePresets}
            />
        </div>
    );
}
