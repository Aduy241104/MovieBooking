import { Button, Modal, Tooltip } from "antd";
import { Bell } from "lucide-react";
import NotificationItem from "./NotificationItem";
import { useNotification } from "../../../context/NotificationContext";

export default function AllNotificationsModal({
    showAllModal,
    setShowAllModal,
    modalTimePreset,
    setModalTimePreset,
    filterModalNotifications,
    handleDeleteAllNotifications,
    modalPage,
    setModalPage,
    modalPageSize,
    timePresets,
}) {
    const { notifications, markAsRead, deleteNotification } = useNotification();
    return (
        <>
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
                            <NotificationItem
                                markAsRead={markAsRead}
                                deleteNotification={deleteNotification}
                                modalPage={modalPage}
                                setModalPage={setModalPage}
                                modalPageSize={modalPageSize}
                                filterModalNotifications={filterModalNotifications}
                            />
                        )}
                    </div>
                </div>
            </Modal>
        </>
    );
}
