import { useState } from "react";
import { verifyOTP } from "../../service/AuthService";
import ErrorNotification from "../../components/ErrorNotification/ErrorNotification";
import { Modal } from "antd";


function VerifyOtpForm({ registerRequest }) {
    const [otp, setOtp] = useState("");
    const [errorMessage, setErrorMesage] = useState("");
    const [isLoading, setLoading] = useState(false);
    const [isSuccessModalOpen, setSuccessModalOpen] = useState(false);

    const handleChangeOtp = (e) => {
        setErrorMesage("");
        const { value } = e.target;
        if (/^\d{0,6}$/.test(value)) {
            setOtp(value);
        }
    };

    const hanleVerifyOTP = async (data) => {

        setLoading(true);
        try {
            const response = await verifyOTP(data);
            setSuccessModalOpen(true);
        } catch (error) {
            setErrorMesage(error.message);
        } finally {
            setLoading(false);
        }
    }

    const handleSubmitOtp = async (e) => {
        e.preventDefault();
        if (otp.length === 6) {
            setLoading(true);
            const payload = {
                verifyOtp: otp,
                registerRequest: registerRequest
            };

           await hanleVerifyOTP(payload);
        } else {
            alert("Vui lòng nhập đủ 6 số OTP");
        }
    };

    const handleSuccessModalOk = () => {
        setSuccessModalOpen(false);
        window.location.href = "/login";
    };

    return (
        <>
            <form className="text-light p-4 rounded shadow-sm" onSubmit={ handleSubmitOtp }>
                <ErrorNotification>{ errorMessage }</ErrorNotification>
                <h4 className="text-center mb-3">Nhập mã OTP</h4>
                <p className="text-center text-light">
                    Mã xác thực đã gửi đến email <strong>{ registerRequest.email }</strong>
                </p>
                <div className="d-flex justify-content-center mb-3">
                    <input
                        type="text"
                        className="form-control text-center fs-4"
                        maxLength={ 6 }
                        value={ otp }
                        onChange={ handleChangeOtp }
                        placeholder="______"
                        style={ { letterSpacing: "10px", maxWidth: "200px" } }
                        required
                    />
                </div>

                <button type="submit" className="btn btn-gardient w-100 mt-4 rounded-4 text-light">
                    { (isLoading) ? (
                        <div className="spinner-border text-light" role="status" style={ { height: '25px', width: '25px' } }>
                            <span className="visually-hidden">Loading...</span>
                        </div>
                    ) : "Xác minh" }
                </button>
            </form>

            <Modal
                title="Xác thực thành công"
                open={ isSuccessModalOpen }
                onOk={ handleSuccessModalOk }
                onCancel={ () => setSuccessModalOpen(false) }
                okText="Đăng nhập"
                cancelText="Đóng"
            >
                <p>Tài khoản của bạn đã được xác thực thành công! Vui lòng đăng nhập để tiếp tục.</p>
            </Modal>
        </>
    );
}

export default VerifyOtpForm;
