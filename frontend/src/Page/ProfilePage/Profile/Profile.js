import { useState, useEffect, useLayoutEffect } from "react";
import { Form, Input, Radio, Button } from "antd";
import Avatar from './Avatar/Avatar';
import { updateProfileAPI, viewPersonalProfileAPI } from '../../../service/ProfileService';
import { openNotification } from "../../../Utils/Notification";
import ChangeEmail from './ChangeEmail'

function Profile() {
    const [form] = Form.useForm();
    const [accountInfor, setAccountInfor] = useState({});
    const [isLoading, setLoading] = useState(false);

    useLayoutEffect(() => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }, [])

    useEffect(() => {
        const fetchAccounAPI = async () => {
            try {
                const res = await viewPersonalProfileAPI();
                setAccountInfor(res.result);
            } catch (error) {
                setAccountInfor({})
            }
        }
        fetchAccounAPI();
    }, []);

    useEffect(() => {
        if (accountInfor && accountInfor.fullName) {
            const { fullName, gender, phoneNumber, dateOfBirth, email } = accountInfor;
            form.setFieldsValue({ fullName, gender, phoneNumber, dateOfBirth, email });
        }
    }, [accountInfor]);

    const handleFinish = async (values) => {
        setLoading(true);
        try {
            const response = await updateProfileAPI(values);
            localStorage.setItem("user", JSON.stringify(response.result));
            openNotification("success", "Cập nhật thành công", "Thông tin tài khoản đã được cập nhật.");

        } catch (err) {
            openNotification("error", "Lỗi cập nhật", "Đã xảy ra lỗi khi cập nhật thông tin.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className='container-fluid'>
            <div className='row pt-4'>
                <div className='col-6'>
                    <h5>Tài khoản</h5>
                    <p className='text-secondary fs-6 pb-4'>Cập nhật thông tin tài khoản</p>
                    <p className='pb-3 fs-6 fw-300'>
                        Điểm tích lũy: <span className='text-warning'>{ accountInfor.score }</span>
                    </p>

                    <Form
                        form={ form }
                        layout="vertical"
                        onFinish={ handleFinish }>

                        <div className='d-flex'>
                            <Form.Item label={ <span className='text-secondary'>Email</span> } name="email" rules={ [
                                { required: false, message: "Vui lòng nhập họ và tên" },
                                { min: 2, message: "Họ và tên tối thiểu 2 ký tự" }
                            ] } className='flex-1'>
                                <Input className='bg-transparent text-light p-2 border-1 border-secondary' readOnly />
                            </Form.Item>
                            <ChangeEmail />
                        </div>

                        <Form.Item label={ <span className='text-secondary'>Họ và tên</span> } name="fullName" rules={ [
                            { required: true, message: "Vui lòng nhập họ và tên" },
                            { min: 2, message: "Họ và tên tối thiểu 2 ký tự" }
                        ] }>
                            <Input className='bg-transparent text-light p-2 border-1 border-secondary' />
                        </Form.Item>

                        <Form.Item label={ <span className='text-secondary'>Số điện thoại</span> } name="phoneNumber" rules={ [
                            { required: true, message: "Vui lòng nhập số điện thoại" },
                            { pattern: /^[0-9]*$/, message: "Số điện thoại chỉ chứa số" },
                            { len: 10, message: "Số điện thoại phải đủ 10 chữ số" }
                        ] }>
                            <Input className='bg-transparent text-light p-2 border-1 border-secondary' />
                        </Form.Item>

                        <Form.Item label={ <span className='text-secondary'>Ngày sinh</span> } name="dateOfBirth" rules={ [
                            { required: true, message: "Vui lòng chọn ngày sinh" }
                        ] }>
                            <Input type="date" className='bg-transparent text-light p-2 border-1 border-secondary' />
                        </Form.Item>

                        <Form.Item label={ <span className='text-secondary'>Giới tính</span> } name="gender" rules={ [
                            { required: true, message: "Vui lòng chọn giới tính" }
                        ] }>
                            <Radio.Group className='text-light'>
                                <Radio value="Male"><span className='text-light'>Nam</span></Radio>
                                <Radio value="Female"><span className='text-light'>Nữ</span></Radio>
                            </Radio.Group>
                        </Form.Item>

                        <Button className='bg-red p-3 ps-4 pe-4 text-black fw-bold' type="primary" htmlType="submit">
                            Cập nhật
                            { isLoading &&
                                <div className="spinner-border spinner-border-sm" role="status">
                                    <span className="visually-hidden">Loading...</span>
                                </div>
                            }
                        </Button>
                    </Form>

                </div>
                <div className='col-6'>
                    <Avatar
                        originalImage={ accountInfor.avatar }
                    />
                </div>
            </div>
        </div>
    )
}

export default Profile