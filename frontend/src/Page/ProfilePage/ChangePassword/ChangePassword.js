import { Form, Input, Button, message } from 'antd';
import { changePasswordAPI } from '../../../service/ProfileService';
import { openNotification } from '../../../Utils/Notification';
import { useState } from 'react';

function ChangePassword() {
    const [form] = Form.useForm();
    const [isLoading, setLoading] = useState(false);

    const handleFinish = async (values) => {
        try {
            setLoading(true);
            const response = await changePasswordAPI(values);

            if (!response.success) {
                if (response.status === 401) {
                    form.setFields([
                        {
                            name: 'oldPassword',
                            errors: ["Mật khẩu hiện tại không đúng"],
                        },
                    ]);
                } else {
                    openNotification("error", "Lỗi", response.message);
                }
                return;
            }
            openNotification("success", "Cập nhật thành công", "Mật khẩu đã được thay đổi.");
            form.resetFields();

        } catch (error) {
            openNotification("error", "Lỗi");
        } finally {
            setLoading(false);

        }
    };


    return (
        <div className='text-light mt-4 w-50 pe-3'>
            <h5>Đổi mật khẩu</h5>

            <Form
                layout="vertical"
                form={ form }
                onFinish={ handleFinish }>

                <Form.Item
                    label={ <span className='text-light'>Mật khẩu hiện tại</span> }
                    name="oldPassword"
                    rules={ [
                        { required: true, message: "Vui lòng nhập mật khẩu hiện tại" }
                    ] }>
                    <Input.Password className='bg-transparent text-light p-2'
                        iconRender={ (visible) => (
                            <span style={ { color: 'white', fontSize: '14px' } }>
                                { visible ? (<i className="fa-solid fa-eye-slash"></i>) : (<i className="fa-solid fa-eye"></i>) }
                            </span>
                        ) } />
                </Form.Item>

                <Form.Item
                    label={ <span className='text-light'>Mật khẩu mới</span> }
                    name="newPassword"
                    rules={ [
                        { required: true, message: "Vui lòng nhập mật khẩu mới" },
                        { min: 6, message: "Mật khẩu tối thiểu 6 ký tự" }
                    ] }>
                    <Input.Password className='bg-transparent text-light p-2'
                        iconRender={ (visible) => (
                            <span style={ { color: 'white', fontSize: '14px' } }>
                                { visible ? (<i className="fa-solid fa-eye-slash"></i>) : (<i className="fa-solid fa-eye"></i>) }
                            </span>
                        ) }
                    />
                </Form.Item>

                <Form.Item
                    label={ <span className='text-light'>Xác nhận mật khẩu</span> }
                    name="confirmNewPass"
                    dependencies={ ['newPassword'] }
                    rules={ [
                        { required: true, message: "Vui lòng xác nhận mật khẩu" },
                        ({ getFieldValue }) => ({
                            validator(_, value) {
                                if (!value || getFieldValue('newPassword') === value) {
                                    return Promise.resolve();
                                }
                                return Promise.reject(new Error('Mật khẩu xác nhận không khớp.'));
                            },
                        })
                    ] }>
                    <Input.Password className='bg-transparent text-light p-2'
                        iconRender={ (visible) => (
                            <span style={ { color: 'white', fontSize: '14px' } }>
                                { visible ? (<i className="fa-solid fa-eye-slash"></i>) : (<i className="fa-solid fa-eye"></i>) }
                            </span>
                        ) }
                    />
                </Form.Item>

                <Button
                    className='bg-red p-2 text-black'
                    type="primary"
                    htmlType="submit">
                    Đổi mật khẩu
                    { isLoading &&
                        <div className="spinner-border spinner-border-sm" role="status">
                            <span className="visually-hidden">Loading...</span>
                        </div>
                    }
                </Button>
            </Form>
            <p className='mt-4 fs-7'>
                Quên mật khẩu, nhấn vào
                <button className='text-red'>đây</button>
            </p>
        </div>
    )
}

export default ChangePassword