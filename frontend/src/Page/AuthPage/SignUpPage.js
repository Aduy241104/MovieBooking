import AuhenticationLayout from '../../layouts/AuthenticationLayout'
import { useState } from 'react'
import VerifyOtpForm from './OtpInput';
import { registerAccountAPI } from '../../service/AuthService';
import ErrorNotification from '../../components/ErrorNotification/ErrorNotification';

function SignUpPage() {
    const [validated, setValidated] = useState(false);
    const [isLoading, setLoading] = useState(false);
    const [errorMess, setError] = useState("");
    const [step, setStep] = useState(1);
    const [registerData, setRegisterData] = useState({
        email: "",
        password: "",
        confirmPassword: "",
        fullName: "",
        gender: "",
        phoneNumber: "",
        dateOfBirth: ""
    });

    const handleChangeData = (e) => {
        const { name, value } = e.target;
        setError("");
        if (value.length > 0 && value.trim() === "") {
            return;
        }
        setRegisterData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const requestOtpApi = async () => {
        setLoading(true);
        try {
            const { confirmPassword, ...dataToSend } = registerData; // bỏ confirmPassword
            await registerAccountAPI(dataToSend);
            setStep(prev => prev + 1);
        } catch (error) {
            setError(error.message);
        } finally {
            setLoading(false);
        }
    };

    const handleRegister = async (event) => {
        event.preventDefault();
        event.stopPropagation();
        const form = event.target;

        if (registerData.password !== registerData.confirmPassword) {
            setError("Mật khẩu và xác nhận mật khẩu không khớp.");
            return;
        }

        if (form.checkValidity()) {
            await requestOtpApi();
        }
        setValidated(true);
    };

    const renderOtpVerify = () => {
        return (
            <VerifyOtpForm registerRequest={ registerData } />
        );
    };

    const renderSignUpForm = () => {
        return (
            <form
                className={ `login-form registers text-light needs-validation h-100 ${validated ? 'was-validated' : ''}` }
                noValidate
                onSubmit={ handleRegister }
            >
                <h4 className='text-center'>Đăng Ký</h4>
                <ErrorNotification>{ errorMess }</ErrorNotification>

                <div className="mb-3">
                    <label className="form-label">Email</label>
                    <input
                        type="text"
                        className="form-control border border-1 border-dark"
                        name="email"
                        value={ registerData.email }
                        required
                        onChange={ handleChangeData }
                    />
                    <div className="invalid-feedback">Vui lòng nhập email hợp lệ.</div>
                </div>

                <div className="mb-3">
                    <label className="form-label">Mật khẩu</label>
                    <input
                        type="password"
                        className="form-control border border-1 border-dark"
                        name="password"
                        value={ registerData.password }
                        pattern="^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$"
                        required
                        onChange={ handleChangeData }
                    />
                    <div className="invalid-feedback"> Mật khẩu phải có ít nhất 8 ký tự, bao gồm cả chữ và số.</div>
                </div>

                <div className="mb-3">
                    <label className="form-label">Xác nhận mật khẩu</label>
                    <input
                        type="password"
                        className="form-control border border-1 border-dark"
                        name="confirmPassword"
                        value={ registerData.confirmPassword }
                       
                        pattern="^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$"
                        required
                        onChange={ handleChangeData }
                    />
                    <div className="invalid-feedback">Mật khẩu xác nhận không khớp.</div>
                </div>

                <div className="mb-3">
                    <label className="form-label">Họ và tên</label>
                    <input
                        type="text"
                        className="form-control border border-1 border-dark"
                        name="fullName"
                        value={ registerData.fullName }
                        required
                        onChange={ handleChangeData }
                    />
                    <div className="invalid-feedback">Vui lòng nhập họ tên.</div>
                </div>

                <div className="mb-3">
                    <label className="form-label">Giới tính</label>
                    <select
                        className="form-control border border-1 border-dark"
                        name="gender"
                        value={ registerData.gender }
                        required
                        onChange={ handleChangeData }
                    >
                        <option value="">-- Chọn giới tính --</option>
                        <option value="Male">Nam</option>
                        <option value="Female">Nữ</option>
                    </select>
                    <div className="invalid-feedback">Vui lòng chọn giới tính.</div>
                </div>

                <div className="mb-3">
                    <label className="form-label">Số điện thoại</label>
                    <input
                        type="tel"
                        className="form-control border border-1 border-dark"
                        name="phoneNumber"
                        value={ registerData.phoneNumber }
                        pattern="^\d{10,11}$"
                        required
                        onChange={ handleChangeData }
                    />
                    <div className="invalid-feedback">Số điện thoại không hợp lệ.</div>
                </div>

                <div className="mb-3">
                    <label className="form-label">Ngày sinh</label>
                    <input
                        type="date"
                        className="form-control border border-1 border-dark"
                        name="dateOfBirth"
                        value={ registerData.dateOfBirth }
                        required
                        onChange={ handleChangeData }
                    />
                    <div className="invalid-feedback">Vui lòng chọn ngày sinh.</div>
                </div>

                <button type="submit" className="btn w-100 text-black" style={ { backgroundColor: "var(--red)" } }>
                    { isLoading ? "Loading..." : "Đăng ký" }
                </button>
            </form>
        );
    };
    return (
        <AuhenticationLayout>
            { (step === 1) ? (renderSignUpForm()) : (renderOtpVerify()) }
        </AuhenticationLayout>
    )
}

export default SignUpPage
