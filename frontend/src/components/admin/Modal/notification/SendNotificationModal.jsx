import { Modal, Form, Input, Select, Button, Table, Checkbox, message } from "antd";
import { useState, useEffect } from "react";
import { sendNotificationAPI, sendNotificationToAllAPI } from "../../../../service/NotificationService";

const { TextArea } = Input;
const { Option } = Select;

export const SendNotificationModal = ({
    isOpen,
    setIsOpen,
    dataUsers,
    userText,
    preselectedUser,
    setPreselectedUser,
}) => {
    const [form] = Form.useForm();
    const [selectedUsers, setSelectedUsers] = useState([]);
    const [loading, setLoading] = useState(false);

    // Khi modal mở và có preselectedUser, tự động chọn user đó
    useEffect(() => {
        if (isOpen && preselectedUser) {
            setSelectedUsers([preselectedUser.accountId]);
        } else if (isOpen && !preselectedUser) {
            setSelectedUsers([]);
        }
    }, [isOpen, preselectedUser]);

    const notificationTypes = [
        { value: "SYSTEM", label: "Hệ thống" },
        { value: "PROMOTION", label: "Khuyến mãi" },
        { value: "MAINTENANCE", label: "Bảo trì" },
        { value: "ANNOUNCEMENT", label: "Thông báo" },
        { value: "WARNING", label: "Cảnh báo" },
    ];

    const handleClose = () => {
        setIsOpen(false);
        form.resetFields();
        setSelectedUsers([]);
        if (setPreselectedUser) {
            setPreselectedUser(null);
        }
    };

    const handleSelectUser = (userId, checked) => {
        if (checked) {
            setSelectedUsers((prev) => [...prev, userId]);
        } else {
            setSelectedUsers((prev) => prev.filter((id) => id !== userId));
        }
    };

    const handleSelectAll = (checked) => {
        if (checked) {
            setSelectedUsers(dataUsers.map((user) => user.accountId));
        } else {
            setSelectedUsers([]);
        }
    };

    const handleSendToSelected = async (values) => {
        if (selectedUsers.length === 0) {
            message.error("Vui lòng chọn ít nhất một người dùng!");
            return;
        }

        setLoading(true);
        try {
            const payload = {
                accountIds: selectedUsers,
                title: values.title,
                content: values.content,
                type: values.type,
            };

            const res = await sendNotificationAPI(payload);
            if (res.result) {
                message.success(`Đã gửi thông báo đến ${selectedUsers.length} người dùng!`);
                handleClose();
            } else {
                message.error(`Lỗi: ${res.message}`);
            }
        } catch (error) {
            message.error("Có lỗi xảy ra khi gửi thông báo!");
        } finally {
            setLoading(false);
        }
    };

    const handleSendToAll = async (values) => {
        setLoading(true);
        try {
            const payload = {
                title: values.title,
                content: values.content,
                type: values.type,
            };

            const res = await sendNotificationToAllAPI(payload);
            if (res.result) {
                message.success(`Đã gửi thông báo đến tất cả ${userText.toLowerCase()}!`);
                handleClose();
            } else {
                message.error(`Lỗi: ${res.message}`);
            }
        } catch (error) {
            message.error("Có lỗi xảy ra khi gửi thông báo!");
        } finally {
            setLoading(false);
        }
    };

    const columns = [
        {
            title: (
                <Checkbox
                    checked={selectedUsers.length === dataUsers.length && dataUsers.length > 0}
                    indeterminate={selectedUsers.length > 0 && selectedUsers.length < dataUsers.length}
                    onChange={(e) => handleSelectAll(e.target.checked)}
                >
                    Tất cả
                </Checkbox>
            ),
            width: 120,
            render: (_, record) => (
                <Checkbox
                    checked={selectedUsers.includes(record.accountId)}
                    onChange={(e) => handleSelectUser(record.accountId, e.target.checked)}
                />
            ),
        },
        {
            title: "Tên",
            dataIndex: "fullName",
            width: 150,
        },
        {
            title: "Email",
            dataIndex: "email",
            ellipsis: true,
        },
        {
            title: "Trạng thái",
            width: 100,
            render: (_, record) => (
                <span className={record.status === 1 ? "text-green-600" : "text-red-600"}>
                    {record.status === 1 ? "Hoạt động" : "Đã khóa"}
                </span>
            ),
        },
    ];

    return (
        <Modal
            title={`Gửi thông báo đến ${userText}`}
            open={isOpen}
            onCancel={handleClose}
            width={800}
            footer={null}
            centered
        >
            <Form form={form} layout="vertical" onFinish={handleSendToSelected}>
                <Form.Item name="title" label="Tiêu đề" rules={[{ required: true, message: "Vui lòng nhập tiêu đề!" }]}>
                    <Input placeholder="Nhập tiêu đề thông báo..." />
                </Form.Item>

                <Form.Item
                    name="content"
                    label="Nội dung"
                    rules={[{ required: true, message: "Vui lòng nhập nội dung!" }]}
                >
                    <TextArea rows={4} placeholder="Nhập nội dung thông báo..." />
                </Form.Item>

                <Form.Item
                    name="type"
                    label="Loại thông báo"
                    rules={[{ required: true, message: "Vui lòng chọn loại thông báo!" }]}
                >
                    <Select placeholder="Chọn loại thông báo">
                        {notificationTypes.map((type) => (
                            <Option key={type.value} value={type.value}>
                                {type.label}
                            </Option>
                        ))}
                    </Select>
                </Form.Item>

                <div className="mb-4">
                    <h4 className="mb-3">Chọn người nhận:</h4>
                    <Table
                        columns={columns}
                        dataSource={dataUsers}
                        rowKey="accountId"
                        size="small"
                        scroll={{ y: 300 }}
                        pagination={{
                            pageSize: 10,
                            showSizeChanger: false,
                            showTotal: (total, range) =>
                                `${range[0]}-${range[1]} trong ${total} ${userText.toLowerCase()}`,
                        }}
                    />
                    <div className="mt-2 text-sm text-gray-600">
                        Đã chọn: <strong>{selectedUsers.length}</strong> / {dataUsers.length} {userText.toLowerCase()}
                    </div>
                </div>

                <div className="flex justify-end gap-3">
                    <Button onClick={handleClose}>Hủy</Button>
                    <Button
                        type="primary"
                        ghost
                        loading={loading}
                        onClick={() => form.validateFields().then(handleSendToAll)}
                    >
                        Gửi tất cả
                    </Button>
                    <Button type="primary" htmlType="submit" loading={loading} disabled={selectedUsers.length === 0}>
                        Gửi cho {selectedUsers.length} người đã chọn
                    </Button>
                </div>
            </Form>
        </Modal>
    );
};
