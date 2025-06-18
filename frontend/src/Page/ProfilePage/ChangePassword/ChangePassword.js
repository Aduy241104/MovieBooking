import { Form, Input, Button, message } from 'antd';

function ChangePassword() {
    const handleFinish = async (values) => {
        try {
            // Gọi API để đổi mật khẩu
            const response = await fetch('/api/user/change-password', {
                method: 'PUT',
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(values),
            });

            if (response.ok) {
                message.success("Đổi mật khẩu thành công!");
            } else {
                message.error("Đổi mật khẩu thất bại!");
            }
        } catch (err) {
            console.error(err);
            message.error("Đã xảy ra lỗi!");
        }
    };

    return (
        <div className='text-light mt-4 w-50 pe-3'>
            <h5>Đổi mật khẩu</h5>

            <Form
                layout="vertical"
                onFinish={ handleFinish }>

                <Form.Item
                    label={ <span className='text-light'>Mật khẩu hiện tại</span> }
                    name="currentPassword"
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
                    className='bg-red p-2 text-light'
                    type="primary"
                    htmlType="submit">
                    Đổi mật khẩu
                </Button>
            </Form>
        </div>
    )
}

export default ChangePassword