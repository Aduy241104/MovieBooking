import { useEffect, useState } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { Badge, Dropdown, List, Button, Empty, Typography, message } from "antd";
import { BellOutlined, CheckOutlined, ClockCircleOutlined } from "@ant-design/icons";
import styles from './NotificationBell.module.scss';
import classNames from "classnames/bind";
import { fetchNotificationsAPI, markNotificationAsReadAPI } from "../../service/NotificationService";
import dayjs from "dayjs";

const cx = classNames.bind(styles);
const { Text } = Typography;

export const NotificationBell = ({ accountId }) => {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        fetchNotificationsAPI()
            .then(res => {
                setNotifications(res);
                setUnreadCount(res.filter(n => !n.isRead).length);
            })
            .catch(err => {
                if (err.message === "Network Error") {
                    message.error("Không thể tải thông báo. Vui lòng thử lại sau.");
                } else {
                    message.error(`Đã xảy ra lỗi khi tải thông báo: ${err.message}. Vui lòng thử lại sau.`);
                }
            });
    }, []);

    useEffect(() => {
        let client;
        try {
            // Tạo client STOMP với SockJS
            client = new Client({
                // Trả về SockJS => dùng để tạo kết nối WebSocket tới server
                webSocketFactory: () => new SockJS("http://localhost:8081/ws-notification"),
                reconnectDelay: 10000,
                onStompError: (frame) => {
                    // Lỗi STOMP protocol
                    message.error("STOMP error:", frame);
                },
                onWebSocketError: (event) => {
                    // Lỗi kết nối WebSocket
                    message.error("WebSocket error:", event);
                }
            });

            // Khi kết nối WebSocket thành công
            client.onConnect = () => {
                client.subscribe(`/queue/notify-${accountId}`, (message) => {
                    console.log("Received notification:", message);
                    const notification = JSON.parse(message.body);
                    setNotifications(prev => [notification, ...prev]);
                    // Tăng số lượng thông báo chưa đọc
                    setUnreadCount(prev => prev + 1);
                });
            };
            // Kích hoạt kết nối tới WebSocket server
            client.activate();

        } catch (error) {
            console.error("Error connecting to WebSocket:", error);
            message.error("Không thể kết nối đến máy chủ thông báo. Vui lòng thử lại sau.");
        }

        return () => {
            if (client) client.deactivate();
        };

    }, [accountId]);

    const handleMarkAsRead = async (id) => {
        await markNotificationAsReadAPI(id);
        setNotifications(notifications.map(n =>
            n.id === id ? { ...n, isRead: true } : n
        ));
        setUnreadCount(notifications.filter(n => !n.isRead && n.id !== id).length);
    };

    const handleMarkAllAsRead = async () => {
        const unreadNotifications = notifications.filter(n => !n.isRead);
        for (const notification of unreadNotifications) {
            await markNotificationAsReadAPI(notification.id);
        }
        setNotifications(notifications.map(n => ({ ...n, isRead: true })));
        setUnreadCount(0);
    };

    const getNotificationIcon = (type) => {
        switch (type) {
            case 'MOVIE':
                return '🎬';
            case 'BOOKING':
                return '🎫';
            case 'REVIEW':
                return '⭐';
            case 'SYSTEM':
                return '🔔';
            default:
                return '📢';
        }
    };

    const formatTime = (createdAt) => {
        const now = dayjs();
        const time = dayjs(createdAt);
        const diffInMinutes = now.diff(time, 'minute');

        // console.log('diffInMinutes', diffInMinutes);

        if (diffInMinutes < 1) return 'Vừa xong';
        if (diffInMinutes < 60) return `${diffInMinutes} phút trước`;
        if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)} giờ trước`;
        return time.format('HH:mm - DD/MM/YYYY');
    };

    const notificationList = (
        <div className={cx('notification-dropdown')}>
            {/* Header */}
            <div className={cx('notification-header')}>
                <div className="d-flex justify-content-between align-items-center">
                    <Text strong style={{ fontSize: '16px' }}>Thông báo</Text>
                    {unreadCount > 0 && (
                        <Button
                            type="text"
                            size="small"
                            onClick={handleMarkAllAsRead}
                            className={cx('mark-all-btn')}
                        >
                            Đánh dấu tất cả đã đọc
                        </Button>
                    )}
                </div>
                {/* <Divider style={{ margin: '8px 0' }} /> */}
            </div>

            {/* Notification List */}
            <div className={cx('notification-list')}>
                {notifications.length === 0 ? (
                    <Empty
                        description="Không có thông báo nào"
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        style={{ padding: '20px 0' }}
                    />
                ) : (
                    <List
                        dataSource={notifications}
                        renderItem={item => (
                            <List.Item
                                className={cx('notification-item', { 'unread': !item.isRead })}
                                style={{ padding: '12px 16px', border: 'none' }}
                            >
                                <div className="d-flex w-100">
                                    {/* Icon */}
                                    <div className={cx('notification-icon')}>
                                        <span style={{ fontSize: '20px' }}>
                                            {getNotificationIcon(item.type)}
                                        </span>
                                    </div>

                                    {/* Content */}
                                    <div className="flex-grow-1 ms-3">
                                        <div className="d-flex justify-content-between align-items-start">
                                            <div className="flex-grow-1">
                                                <Text
                                                    strong={!item.isRead}
                                                    className={cx('notification-title')}
                                                >
                                                    {item.title}
                                                </Text>
                                                <div className={cx('notification-content')}>
                                                    <Text type="secondary" style={{ fontSize: '14px' }}>
                                                        {item.content}
                                                    </Text>
                                                </div>
                                                <div className={cx('notification-time')}>
                                                    <ClockCircleOutlined style={{ fontSize: '12px', marginRight: '4px' }} />
                                                    <Text type="secondary" style={{ fontSize: '12px' }}>
                                                        {formatTime(item.createdAt)}
                                                    </Text>
                                                </div>
                                            </div>

                                            {/* Actions */}
                                            <div className={cx('notification-actions')}>
                                                {!item.isRead && (
                                                    <Button
                                                        type="text"
                                                        size="small"
                                                        icon={<CheckOutlined />}
                                                        onClick={() => handleMarkAsRead(item.id)}
                                                        className={cx('mark-read-btn')}
                                                        title="Đánh dấu đã đọc"
                                                    />
                                                )}
                                                {!item.isRead && (
                                                    <div className={cx('unread-dot')} />
                                                )}
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
                <div className={cx('notification-footer')}>
                    <Button type="text" block>
                        Xem tất cả thông báo
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
            onOpenChange={setVisible}
            placement="bottomRight"
            overlayClassName={cx('notification-overlay')}
        >
            <div className={cx('notification-bell')}>
                <Badge
                    count={unreadCount}
                    overflowCount={99}
                    className={cx('notification-badge')}
                >
                    <BellOutlined className={cx('bell-icon')} />
                </Badge>
            </div>
        </Dropdown>
    );
};