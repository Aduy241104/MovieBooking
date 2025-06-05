import { Col, DatePicker, Form, Input, Modal, notification, Row, Select } from "antd";
import dayjs from 'dayjs';
import { useEffect } from "react";
import { updateAccountQuickAPI } from "../../../service/AccountService";
import { useOutletContext } from "react-router-dom";



export const UpdateUserModal = (props) => {
    const { setRefreshFlag } = useOutletContext();

    const { isUpdateModalOpen, setIsUpdateModalOpen, dataUser, setDataUser, userText } = props;
    const [form] = Form.useForm();

    useEffect(() => {
        if (dataUser && dataUser.accountId) {
            form.setFieldsValue({
                fullName: dataUser.fullName,
                gender: dataUser.gender,
                phoneNumber: dataUser.phoneNumber,
                dateOfBirth: dataUser.dateOfBirth ? dayjs(dataUser.dateOfBirth) : null
            });
        }
    }, [dataUser])


    const handleSubmit = async (values) => {
        const dataToUpdate = {
            ...values,
            dateOfBirth: values.dateOfBirth ? dayjs(values.dateOfBirth).format("YYYY-MM-DD") : null,
        };
        const res = await updateAccountQuickAPI(
            dataUser.accountId,
            dataToUpdate
        );
        if (res.result) {
            notification.success({
                message: "CẬP NHẬT THÀNH CÔNG",
                description: `Cập nhật ${userText.toLowerCase()} thành công`
            });
            setRefreshFlag(prev => !prev);
            setIsUpdateModalOpen(false);
            return;
        }
        notification.error({
            message: "CẬP NHẬT THẤT BẠI",
            description: `ERROR: ${res.message}`
        });
    };

    return (
        <>
            <Modal
                title={`CẬP NHẬT ${userText.toUpperCase()}`}
                open={isUpdateModalOpen}
                onOk={() => form.submit()}
                onCancel={() => setIsUpdateModalOpen(false)}
                afterClose={() => {
                    form.resetFields();
                    setDataUser("");
                }}
                okText={"Lưu thay đổi"}
                cancelText={"Huỷ"}
                maskClosable={false}
            >
                <Form form={form}
                    layout="vertical"
                    onFinish={handleSubmit}
                >
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