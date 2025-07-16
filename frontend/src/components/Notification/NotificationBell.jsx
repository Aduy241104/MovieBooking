import { useEffect, useState } from "react";
import { Badge, Dropdown, List, Button, Empty, Typography } from "antd";
import { BellOutlined, CheckOutlined, ClockCircleOutlined } from "@ant-design/icons";
import styles from "./NotificationBell.module.scss";
import classNames from "classnames/bind";
import { useNotification } from "../../context/NotificationContext";
import dayjs from "dayjs";
import { Link } from "react-router-dom";

const cx = classNames.bind(styles);
const { Text } = Typography;

export const NotificationBell = ({ accountId }) => {
    const { notifications, unreadCount, fetchNotifications, markAsRead, markAllAsRead, setupWebSocket } =
        useNotification();
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        fetchNotifications();
    }, [fetchNotifications]);

    useEffect(() => {
        let client;
        if (!accountId) return;

        client = setupWebSocket(accountId);

        return () => {
            if (client) client.deactivate();
        };
    }, [accountId, setupWebSocket]);

    const handleDropdownOpen = (open) => {
        setVisible(open);
        if (open) {
            fetchNotifications();
        }
    };

    const handleMarkAsRead = async (id) => {
        await markAsRead(id);
    };

    const handleMarkAllAsRead = async () => {
        await markAllAsRead();
    };

    const getNotificationIcon = (type) => {
        switch (type) {
            case "MOVIE":
                return "🎬";
            case "BOOKING":
                return "🎫";
            case "REVIEW":
                return "⭐";
            case "SYSTEM":
                return "🔔";
            default:
                return "📢";
        }
    };

    const formatTime = (createdAt) => {
        const now = dayjs();
        const time = dayjs(createdAt);
        const diffInMinutes = now.diff(time, "minute");

        // console.log('diffInMinutes', diffInMinutes);

        if (diffInMinutes < 1) return "Vừa xong";
        if (diffInMinutes < 60) return `${diffInMinutes} phút trước`;
        if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)} giờ trước`;
        return time.format("HH:mm - DD/MM/YYYY");
    };

    const notificationList = (
        <div className={cx("notification-dropdown")}>
            {/* Header */}
            <div className={cx("notification-header")}>
                <div className="d-flex justify-content-between align-items-center">
                    <Text strong style={{ fontSize: "16px" }}>
                        Thông báo
                    </Text>
                    {unreadCount > 0 && (
                        <Button type="text" size="small" onClick={handleMarkAllAsRead} className={cx("mark-all-btn")}>
                            Đánh dấu tất cả đã đọc
                        </Button>
                    )}
                </div>
                {/* <Divider style={{ margin: '8px 0' }} /> */}
            </div>

            {/* Notification List */}
            <div className={cx("notification-list")}>
                {notifications.length === 0 ? (
                    <Empty
                        description="Không có thông báo nào"
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        style={{ padding: "20px 0" }}
                    />
                ) : (
                    <List
                        dataSource={notifications}
                        renderItem={(item) => (
                            <List.Item
                                className={cx("notification-item", { unread: !item.isRead })}
                                style={{ padding: "12px 16px", border: "none" }}
                            >
                                <div className="d-flex w-100">
                                    {/* Icon */}
                                    <div className={cx("notification-icon")}>
                                        <span style={{ fontSize: "20px" }}>{getNotificationIcon(item.type)}</span>
                                    </div>

                                    {/* Content */}
                                    <div className="flex-grow-1 ms-3">
                                        <div className="d-flex justify-content-between align-items-start">
                                            <div className="flex-grow-1">
                                                <Text strong={!item.isRead} className={cx("notification-title")}>
                                                    {item.title}
                                                </Text>
                                                <div className={cx("notification-content")}>
                                                    <Text type="secondary" style={{ fontSize: "14px" }}>
                                                        {item.content}
                                                    </Text>
                                                </div>
                                                <div className={cx("notification-time")}>
                                                    <ClockCircleOutlined
                                                        style={{ fontSize: "12px", marginRight: "4px" }}
                                                    />
                                                    <Text type="secondary" style={{ fontSize: "12px" }}>
                                                        {formatTime(item.createdAt)}
                                                    </Text>
                                                </div>
                                            </div>

                                            {/* Actions */}
                                            <div className={cx("notification-actions")}>
                                                {!item.isRead && (
                                                    <Button
                                                        type="text"
                                                        size="small"
                                                        icon={<CheckOutlined />}
                                                        onClick={() => handleMarkAsRead(item.id)}
                                                        className={cx("mark-read-btn")}
                                                        title="Đánh dấu đã đọc"
                                                    />
                                                )}
                                                {!item.isRead && <div className={cx("unread-dot")} />}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </List.Item>
                        )}
                    />
                )}
            </div>

            {/* Footer */}
            {notifications.length > 0 && (
                <div className={cx("notification-footer")}>
                    <Button type="text" block>
                        <Link to="/profile/notifications">Xem tất cả thông báo</Link>
                    </Button>
                </div>
            )}
        </div>
    );

    return (
        <Dropdown
            popupRender={() => notificationList}
            trigger={["click"]}
            open={visible}
            onOpenChange={handleDropdownOpen}
            placement="bottomRight"
            overlayClassName={cx("notification-overlay")}
        >
            <div className={cx("notification-bell")}>
                <Badge count={unreadCount} overflowCount={99} className={cx("notification-badge")}>
                    <BellOutlined className={cx("bell-icon")} />
                </Badge>
            </div>
        </Dropdown>
    );
};
