// UpdatePaymentMethodModal.js
import React, { useEffect } from 'react';
import { Modal, Form, Input, Select, message } from 'antd';
import axios from 'axios';

const BASE_URL = 'http://localhost:8081/api/admin/payment-methods';

const UpdatePaymentMethodModal = ({ visible, setVisible, methodId, setRefreshFlag }) => {
    const [form] = Form.useForm();

    // useEffect(() => {
    //     const fetchDetail = async () => {
    //         try {
    //             const res = await axios.get(`${BASE_URL}`);
    //             const found = res.data.find(m => m.id === methodId);
    //             if (found) form.setFieldsValue(found);
    //         } catch (e) {
    //             message.error("Không lấy được thông tin phương thức.");
    //         }
    //     };
    //     if (methodId) fetchDetail();
    // }, [methodId]);

    const handleUpdate = async () => {
        try {
            const values = await form.validateFields();
            const token = localStorage.getItem('token');

            await axios.put(`${BASE_URL}/${methodId}`, values, {
                headers: { Authorization: `Bearer ${token}` }
            });

            message.success('Cập nhật thành công!');
            setRefreshFlag(prev => !prev);
            setVisible(false);
        } catch (err) {
            console.error("Lỗi khi cập nhật:", err);

            const backendMessage = err?.response?.data;

            // Nếu backend trả về đối tượng có message
            if (backendMessage?.message?.includes("Tên phương thức thanh toán đã tồn tại")) {
                message.error(backendMessage.message);
            }
            // Nếu backend trả về chuỗi đơn giản
            else if (typeof backendMessage === 'string' && backendMessage.includes("Tên phương thức thanh toán đã tồn tại")) {
                message.error(backendMessage);
            }
            else {
                message.error("Cập nhật thất bại.");
            }
        }
    };

    return (
        <Modal
            open={visible}
            onCancel={() => setVisible(false)}
            onOk={handleUpdate}
            okText="Lưu"
            cancelText="Hủy"
            title="Chỉnh sửa phương thức"
        >
            <Form layout="vertical" form={form}>
                <Form.Item name="name" label="Tên phương thức" rules={[{ required: true, message: 'Vui lòng nhập tên phương thức' }]}>
                    <Input />
                </Form.Item>
                <Form.Item name="description" label="Mô tả" rules={[{ required: true, message: 'Vui lòng nhập mô tả của phương thức' }]}>
                    <Input.TextArea rows={2} />
                </Form.Item>
                {/* <Form.Item name="active" label="Trạng thái">
                    <Select>
                        <Select.Option value={true}>Kích hoạt</Select.Option>
                        <Select.Option value={false}>Tạm ngưng</Select.Option>
                    </Select>
                </Form.Item> */}
            </Form>
        </Modal>
    );
};

export default UpdatePaymentMethodModal;
