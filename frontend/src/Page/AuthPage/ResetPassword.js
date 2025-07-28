import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Form, Input, Button, message } from 'antd';
import AuhenticationLayout from '../../layouts/AuthenticationLayout';
import { resetPasswordAPI } from '../../service/AuthService';
import { openNotification } from '../../Utils/Notification';

function ResetPassword() {
    const [form] = Form.useForm();
    const [params] = useSearchParams();
    const [isLoading, setLoading] = useState(false);

    const email = params.get("email");
    const otp = params.get("otp");

    useEffect(() => {
        if (!email || !otp) {
            message.error("Liên kết không hợp lệ!");
        }
    }, [email, otp]);

    const handleReset = async (values) => {
        setLoading(true);
        try {
            const data = {
                email,
                otp,
                newPass: values.newPassword
            }
             await resetPasswordAPI(data);

            openNotification("success", "Đã đặt lại mật khẩu", "Mật khẩu của bạn đã được thay đổi");
        } catch (error) {
            openNotification("error", "Lỗi", error.message);
        } finally {
            setLoading(false)
        }
    };

    return (
        <AuhenticationLayout>
            <div className="text-light mx-auto">
                <h2 className='pb-4'>Đặt lại mật khẩu</h2>

                <Form form={ form } layout="vertical" onFinish={ handleReset }>
                    <Form.Item
                        label={ <span className="text-light">Mật khẩu mới</span> }
                        name="newPassword"
                        rules={ [
                            { required: true, message: "Vui lòng nhập mật khẩu mới" },
                            { min: 6, message: "Mật khẩu phải có ít nhất 6 ký tự" },
                        ] }
                    >
                        <Input.Password className="bg-transparent text-light p-2" />
                    </Form.Item>

                    <Form.Item
                        label={ <span className="text-light">Xác nhận mật khẩu</span> }
                        name="confirmPassword"
                        dependencies={ ['newPassword'] }
                        rules={ [
                            { required: true, message: "Vui lòng xác nhận mật khẩu" },
                            ({ getFieldValue }) => ({
                                validator(_, value) {
                                    if (!value || getFieldValue('newPassword') === value) {
                                        return Promise.resolve();
                                    }
                                    return Promise.reject(new Error("Mật khẩu xác nhận không khớp"));
                                },
                            }),
                        ] }
                    >
                        <Input.Password className="bg-transparent text-light p-2" />
                    </Form.Item>

                    <Button htmlType="submit" className="bg-red text-dark fw-bold p-2">
                        Đặt lại mật khẩu
                        { isLoading &&
                            <div className="spinner-border spinner-border-sm" role="status">
                                <span className="visually-hidden">Loading...</span>
                            </div>
                        }
                    </Button>
                </Form>

                <Button htmlType="submit" className="bg-white text-dark fw-bold p-2 mt-3">
                  <Link to={'/login'}>Đăng nhập</Link>
                </Button>
            </div>
        </AuhenticationLayout>
    );
}

export default ResetPassword;
