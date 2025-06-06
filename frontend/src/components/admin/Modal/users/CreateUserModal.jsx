import { Col, DatePicker, Form, Input, Modal, notification, Row, Select } from "antd";
import { createAccountAPI } from "../../../../service/AccountService";
import { useOutletContext } from "react-router-dom";


export const CreateUserModal = (props) => {
    const { setRefreshFlag } = useOutletContext();

    const { isCreateModalOpen, setIsCreateModalOpen, userText, userRole } = props;
    const [form] = Form.useForm();

    const roleId = userRole === 'Member' ? 3 : userRole === 'Employee' ? 2 : 1

    const handleSubmit = async (values) => {
        console.log('Values: ', roleId, values, userRole);
        const res = await createAccountAPI(
            roleId,
            values.email,
            values.fullName,
            values.gender,
            values.password,
            values.phoneNumber,
            values.dateOfBirth.format('YYYY-MM-DD')
        );
        if (res.result) {
            notification.success({
                message: "THÊM THÀNH CÔNG",
                description: `Thêm mới ${userText.toLowerCase()} thành công`
            });
            setRefreshFlag(prev => !prev);
            setIsCreateModalOpen(false);
            return;
        }
        notification.error({
            message: "THÊM THẤT BẠI",
            description: `ERROR: ${res.message}`
        });
    };

    return (
        <>
            <Modal
                title={`THÊM ${userText.toUpperCase()}`}
                open={isCreateModalOpen}
                onOk={() => form.submit()}
                onCancel={() => setIsCreateModalOpen(false)}
                afterClose={() => form.resetFields()}
                okText={"Thêm mới"}
                cancelText={"Huỷ"}
                maskClosable={false}
            >
                <Form form={form}
                    layout="vertical"
                    onFinish={handleSubmit}
                    initialValues={{
                        gender: "Nam",
                    }}
                >
                    <Form.Item
                        label="Email"
                        name="email"
                        rules={[{ required: true, message: 'Please input your email!' }]}
                    >
                        <Input />
                    </Form.Item>

                    <Form.Item
                        label="Mật khẩu"
                        name="password"
                        rules={[{ required: true, message: 'Please input your password!' }]}
                    >
                        <Input.Password />
                    </Form.Item>

                    <Row justify={"space-between"}>
                        <Col lg={16}>
                            <Form.Item
                                label="Họ và tên"
                                name="fullName"
                                rules={[{ required: true, message: 'Please input your full name!' }]}
                            >
                                <Input />
                            </Form.Item>
                        </Col>

                        <Col lg={6}>
                            <Form.Item
                                label="Giới tính"
                                name="gender"
                            >
                                <Select
                                    options={[
                                        { value: 'Nam', label: 'Nam' },
                                        { value: 'Nữ', label: 'Nữ' },
                                        { value: 'Khác', label: 'Khác' },
                                    ]}
                                    placeholder="Giới tính"
                                />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item
                        label="Số điện thoại"
                        name="phoneNumber"
                        rules={[{ required: true, message: 'Please input your phone!' }]}
                    >
                        <Input />
                    </Form.Item>

                    <Form.Item
                        label="Sinh nhật"
                        name="dateOfBirth"
                        rules={[{ required: true, message: 'Please input your birthday!' }]}
                    >
                        <DatePicker />
                    </Form.Item>
                </Form>
            </Modal>
        </>
    );
}