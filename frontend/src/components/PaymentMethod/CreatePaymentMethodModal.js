import React from 'react';
import { Modal, Form, Input, Select, message } from 'antd';
import axios from 'axios';


const BASE_URL = 'http://localhost:8081/api/admin/payment-methods/create'; // API backend của bạn

const CreatePaymentMethodModal = ({ isCreateModalOpen, setIsCreateModalOpen, setRefreshFlag }) => {
    const [form] = Form.useForm();

    const handleOk = async () => {
        try {
            const values = await form.validateFields();
            console.log("Giá trị tạo mới:", values);

            const token = localStorage.getItem('token');
            console.log("TOKEN:", token);

            await axios.post(BASE_URL, {
                name: values.name,
                description: values.description || '',
                active: values.active,
            }, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });

            message.success('Thêm phương thức thành công!');
            setRefreshFlag(prev => !prev);
            setIsCreateModalOpen(false);
            form.resetFields();
        } catch (err) {
            console.error("Lỗi validate hoặc API:", err);
            message.error('Thêm phương thức thất bại!');
        }
    };

    return (
        <Modal
            title="Thêm phương thức"
            open={isCreateModalOpen}
            onCancel={() => setIsCreateModalOpen(false)}
            onOk={handleOk}
            okText="Thêm"
            cancelText="Hủy"
        >
            <Form layout="vertical" form={form}>
                <Form.Item
                    label="Tên phương thức"
                    name="name"
                    rules={[{ required: true, message: 'Vui lòng nhập tên phương thức' }]}
                >
                    <Input />
                </Form.Item>

                <Form.Item label="Mô tả" name="description">
                    <Input.TextArea rows={2} />
                </Form.Item>

                <Form.Item label="Trạng thái" name="active" initialValue={true}>
                    <Select>
                        <Select.Option value={true}>Kích hoạt</Select.Option>
                        <Select.Option value={false}>Tạm ngưng</Select.Option>
                    </Select>
                </Form.Item>
            </Form>
        </Modal>
    );
};

export default CreatePaymentMethodModal;
