import { Button, Tooltip } from "antd";
import { DeleteOutlined } from "@ant-design/icons";
import { Bell } from "lucide-react";
import { format } from "date-fns";
import { useNotification } from "../../../context/NotificationContext";

export default function NotificationPopup({ setShowAllModal }) {
    const { notifications, unreadCount, isLoading, markAllAsRead, markAsRead, deleteNotification } = useNotification();
    return (
        <>
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
                                onClick={markAllAsRead}
                            >
                                Đánh dấu tất cả đã đọc
                            </button>
                        )}
                    </div>
                </div>

                {/* Danh sách notification */}
                <div className="max-h-80 overflow-y-auto">
                    {isLoading ? (
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
                                        <div className="flex-1 min-w-0" onClick={() => !n.isRead && markAsRead(n.id)}>
                                            <p
                                                className={`text-sm leading-5 ${
                                                    !n.isRead ? "text-gray-900 font-medium" : "text-gray-700"
                                                }`}
                                            >
                                                {n.title}
                                            </p>
                                            <p className="text-xs text-gray-500 mt-1">
                                                {n.createdAt ? format(new Date(n.createdAt), "dd/MM/yyyy HH:mm a") : ""}
                                            </p>
                                            <p className="text-sm text-gray-700 mt-1">{n.content}</p>
                                        </div>
                                        <Tooltip title="Xoá thông báo">
                                            <Button
                                                onClick={() => deleteNotification(n.id)}
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
        </>
    );
}
