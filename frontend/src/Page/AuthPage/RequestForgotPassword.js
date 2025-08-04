import { Form, Input, Button} from 'antd';
import AuhenticationLayout from '../../layouts/AuthenticationLayout';
import { requestForgotPasswordAPI } from '../../service/AuthService';
import { openNotification } from '../../Utils/Notification';
import { useState } from 'react';

function RequestForgotPassword() {
    const [form] = Form.useForm();
    const [isLoading, setLoading] = useState(false);

    const handleSendEmail = async (values) => {
        setLoading(true);
        try {

            await requestForgotPasswordAPI(values);
            openNotification("success", "Email đã gởi", "Email thay đổi mật khẩu đã được gởi");
        } catch (error) {

            openNotification("error", "Lỗi", error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <AuhenticationLayout>
                <div className="text-light mx-auto mt-2">
                    <p className='pb-3'>Nhập email, chúng tôi sẽ gửi liên kết đặt lại mật khẩu cho bạn.</p>
                    <Form form={ form } layout="vertical" onFinish={ handleSendEmail }>
                        <Form.Item
                            label={ <span className='text-light'>Email</span> }
                            name="email"
                            rules={ [
                                { required: true, message: "Vui lòng nhập email" },
                                { type: "email", message: "Email không hợp lệ" },
                            ] }
                        >
                            <Input className="bg-transparent text-light p-2" />
                        </Form.Item>
                        <Button type="primary" htmlType="submit" className="bg-red text-dark p-2">
                            Gửi liên kết đặt lại mật khẩu
                            { isLoading &&
                                <div className="spinner-border spinner-border-sm" role="status">
                                    <span className="visually-hidden">Loading...</span>
                                </div>
                            }
                        </Button>
                    </Form>
                </div>
            </AuhenticationLayout>
        </>
    );
}

export default RequestForgotPassword