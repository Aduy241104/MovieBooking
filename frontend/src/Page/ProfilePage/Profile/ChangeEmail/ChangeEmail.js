import { Modal, Form, Input, Button } from 'antd';
import { useState } from 'react';
import OtpForm from './OtpForm';
import { CheckCircleOutlined } from '@ant-design/icons';
import { requestChangeEmail } from '../../../../service/ProfileService';


function ChangeEmail() {
    const [form] = Form.useForm();
    const [isOpen, setOpen] = useState(false);
    const [isLoading, setLoading] = useState(false);
    const [step, setStep] = useState(0);


    const handleCloseModal = () => {
        setOpen(false);
        form.resetFields();
        setStep(0);
    };

    const handleFinish = async (values) => {
        try {
            setLoading(true);
            await requestChangeEmail(values);
            setStep(2);
        } catch (error) {
            form.setFields([{
                name: 'newEmail',
                errors: [error.message]
            }])
        } finally {
            setLoading(false);
        }
    }
    return (
        <>
            <Modal
                open={ isOpen }
                onCancel={ handleCloseModal }
                footer={ null }
                title={ null }
                closeIcon={ true }
                classNames="modal-trailer"
                style={ { top: 200, backgroundColor: 'black' } }
                width={ 500 }
                maskClosable={ false }  
            >
                { step === 0 && (
                    <div>
                        <h4 className='fw-bold'>Nhập Email bạn muốn thay đổi</h4>
                        <Form
                            form={ form }
                            layout="vertical"
                            onFinish={ handleFinish }>
                            <div className='d-flex mt-3'>
                                <Form.Item
                                    label={ <span className='text-secondary'>Email</span> }
                                    name="newEmail"
                                    rules={ [
                                        { required: true, message: "Vui lòng nhập email mới" },
                                        { type: 'email', message: "Email không hợp lệ" }
                                    ] }
                                    className='flex-1'
                                >
                                    <Input className='bg-transparent text-black p-2 border-1 border-secondary' />
                                </Form.Item>
                            </div>
                            <Button className='bg-red p-3 ps-4 pe-4 text-black fw-bold' type="primary" htmlType="submit">
                                Thay đổi
                                { isLoading &&
                                    <div className="spinner-border spinner-border-sm" role="status">
                                        <span className="visually-hidden">Loading...</span>
                                    </div>
                                }
                            </Button>
                        </Form>
                    </div>
                ) }

                { step === 2 && (
                    <OtpForm newEmail={ form.getFieldValue("newEmail") } nextStep={ setStep } />
                ) }

                { step === 3 && (
                    <div className="text-center p-4" style={ { maxWidth: 400, margin: '50px auto' } }>
                        <CheckCircleOutlined style={ { fontSize: 64, color: 'green' } } />
                        <h3 className='mt-3 text-success fw-bold'>Thay đổi email thành công</h3>
                        <Button type="primary" className="mt-3" onClick={ handleCloseModal }>Tiếp tục</Button>
                    </div>
                ) }
            </Modal>

            <p
                onClick={ () => setOpen(true) }
                style={ { lineHeight: '100px' } }
                className='ms-3 cursor-pointer text-red'
            >
                Thay đổi email
            </p>
        </>
    )
}
export default ChangeEmail