import { DeleteOutlined } from "@ant-design/icons";
import { Button, Tooltip } from "antd";
import { format } from "date-fns";

export default function NotificationItem({
    markAsRead,
    deleteNotification,
    modalPage,
    setModalPage,
    modalPageSize,
    filterModalNotifications,
}) {
    return (
        <>
            {filterModalNotifications()
                .slice(0, modalPage * modalPageSize)
                .map((n) => (
                    <div
                        key={n.id}
                        className={`p-3 hover:bg-gray-50 transition-colors duration-150 cursor-pointer ${
                            !n.isRead ? "bg-blue-50/50 border-l-4 border-blue-400" : ""
                        }`}
                        onClick={() => !n.isRead && markAsRead(n.id)}
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
    );
}
