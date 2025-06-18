import ProfileLayout from '../../../layouts/ProfileLayout'
import { useState, useEffect } from "react";
import { Form, Input, Select, Radio, Button } from "antd";
import Avatar from './Avatar/Avatar';
import { viewPersonalProfileAPI } from '../../../service/ProfileService';


function Profile() {
    const [form] = Form.useForm();
    const [accountInfor, setAccountInfor] = useState({});
    const [isLoading, setLoading] = useState(false);


    const testAPI = async () => {
        const res = await viewPersonalProfileAPI();
        console.log(res);
        const fetched = {
            email: "anhduy@gmail.com",
            fullName: "Anh Duy",
            gender: "Female",
            phoneNumber: "0901231289",
            dateOfBirth: "1990-05-20",
        };
        setAccountInfor(fetched);

    }
    useEffect(() => {

        testAPI()


    }, []);

    useEffect(() => {
        if (accountInfor && accountInfor.fullName) { // account là dữ từ API
            // Lấy ra các field bạn quan tâm
            const { fullName, gender, phoneNumber, dateOfBirth, email } = accountInfor;

            form.setFieldsValue({ fullName, gender, phoneNumber, dateOfBirth, email });
        }
    }, [accountInfor]);

    const handleFinish = async (values) => {
        try {
            const response = await fetch("/api/user/update", {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(values),
            });

            if (response.ok) {
                message.success("Cập nhật thông tin thành công!");
            } else {
                message.error("Cập nhật thất bại!");
            }
        } catch (err) {
            console.error(err);
            message.error("Đã xảy ra lỗi!");
        }
    };

    return (
        <div className='container-fluid'>
            <div className='row pt-4'>
                <div className='col-6'>
                    <h5>Tài khoản</h5>
                    <p className='text-secondary fs-6 pb-4'>Cập nhật thông tin tài khoản</p>
                    <p className='pb-3 fs-6 fw-300'>
                        Điểm tích lũy: <span className='text-warning'>1500</span>
                    </p>

                    <Form
                        form={ form }
                        layout="vertical"
                        onFinish={ handleFinish }>


                        <div className='d-flex'>
                            <Form.Item label={ <span className='text-secondary'>Email</span> } name="email" rules={ [
                                { required: true, message: "Vui lòng nhập họ và tên" },
                                { min: 2, message: "Họ và tên tối thiểu 2 ký tự" }
                            ] } className='flex-1'>
                                <Input className='bg-transparent text-light p-2 border-1 border-secondary' readOnly />
                            </Form.Item>

                            <p
                                style={ { lineHeight: '100px' } }
                                onClick={ () => { console.log("hello") } }
                                className='ms-3 cursor-pointer text-red'
                            >
                                Thay đổi Email
                            </p>
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
                        originalImage={ "https://p16-sign-va.tiktokcdn.com/tos-maliva-avt-0068/a0e63af2063dccd1389e1bc27ee465ba~tplv-tiktokx-cropcenter:1080:1080.jpeg?dr=14579&refresh_token=76b54e80&x-expires=1749092400&x-signature=L%2FIqvwELh%2BmxK9fobJMEfORbNys%3D&t=4d5b0474&ps=13740610&shp=a5d48078&shcp=81f88b70&idc=my" }
                    />
                </div>
            </div>
        </div>
    )
}

export default Profile