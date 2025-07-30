import React, { useRef, useState } from 'react';
import { Form, Input, Button, Space, message } from 'antd';
import { confirmChangeEmailAPI } from '../../../../service/ProfileService';

function OtpForm({ newEmail, nextStep }) {
    const inputRefs = useRef([]);
    const [isLoading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const [form] = Form.useForm();

    const focusNext = (index) => {
        if (index < 5) {
            inputRefs.current[index + 1]?.focus();
        }
    };

    const focusPrev = (index) => {
        if (index > 0) {
            inputRefs.current[index - 1]?.focus();
        }
    };

    const handleChange = (e, index) => {
        const value = e.target.value;
        if (/^\d$/.test(value)) {
            form.setFieldsValue({ [`digit${index}`]: value });
            focusNext(index);
        } else if (value === '') {
            form.setFieldsValue({ [`digit${index}`]: '' });
        }
    };

    const handleKeyDown = (e, index) => {
        if (e.key === 'Backspace') {
            const value = form.getFieldValue(`digit${index}`);
            if (!value) {
                focusPrev(index);
            }
        }
    };

    const onFinish = async (values) => {
        const otp = Object.values(values).join('');
        const email = newEmail;

        const data = {
            otp: otp,
            newEmail: email
        };
        try {
            setLoading(true);
            const response = await confirmChangeEmailAPI(data);
            localStorage.setItem("user", JSON.stringify(response.result));
            nextStep(3);
        } catch (error) {
            setError(error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Form
            form={ form }
            onFinish={ onFinish }
            style={ { maxWidth: 400, margin: '50px auto' } }
        >
            <Form.Item label="Nhập mã OTP" style={ { marginBottom: 24 } }>
                <Space>
                    { [...Array(6)].map((_, index) => (
                        <Form.Item
                            key={ index }
                            name={ `digit${index}` }
                            rules={ [{ required: true, message: '' }] }
                            style={ { marginBottom: 0 } }
                        >
                            <Input
                                maxLength={ 1 }
                                ref={ (el) => (inputRefs.current[index] = el) }
                                style={ {
                                    width: 40,
                                    textAlign: 'center',
                                    fontSize: '20px',
                                    border: '1px solid #ccc'
                                } }
                                onChange={ (e) => handleChange(e, index) }
                                onKeyDown={ (e) => handleKeyDown(e, index) }
                            />
                        </Form.Item>
                    )) }
                </Space>
            </Form.Item>

            <p className='text-danger'>{ error }</p>
            <Form.Item>
                <Button
                    type="primary"
                    htmlType="submit"
                    className="bg-red"
                    block
                >
                    Xác nhận
                </Button>
            </Form.Item>
        </Form>
    );
}

export default OtpForm