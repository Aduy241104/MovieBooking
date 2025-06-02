import { Link, useNavigate } from "react-router-dom";
import AuhenticationLayout from "../layouts/AuthenticationLayout";
import { useState } from "react";
import ErrorNotification from "../components/ErrorNotification/ErrorNotification";

function LoginPage() {
    const [loginData, setLoginData] = useState({
        username: "",
        password: ""
    });

    const [showLoginFail, setShowLoginFail] = useState("");
    const [isLoading, setLoading] = useState(false);

    const navigate = useNavigate();

    const handleChangeLoginData = (e) => {
        const { name, value } = e.target;
        if (loginData[name].length === 0 && value.startsWith(" ")) {
            return;
        }
        setShowLoginFail(false);
        setLoginData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleFetch = async () => {
        setLoading(true);
        try {
            const response = await fetch("http://localhost:8081/api/auth/login-oauth", {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(loginData)
            })

            const res = await response.json();

            if (!res.result.authenticated) {
                setShowLoginFail("Tài khoản hoặc mật khẩu không đúng.");
            } else {
                console.log(res);
                navigate('/home');
            }
        } catch (error) {
            setShowLoginFail("Không thế kết nối đến máy chủ. Vui lòng thử lại sau.");
        } finally {
            setLoading(false);
        }
    }

    const handleSubmit = (e) => {
        e.preventDefault();
        const form = e.target;
        if (!form.checkValidity()) {
            e.stopPropagation();
            form.classList.add("was-validated");
            return;
        }
        handleFetch();
    };

    return (
        <AuhenticationLayout>
            <form className="needs-validation" noValidate onSubmit={ handleSubmit }>
                <h2 className="text-left pb-4 pt-2">Đăng nhập</h2>
                <ErrorNotification>{ showLoginFail }</ErrorNotification>
                <div className="mb-5 form-input">
                    <input
                        placeholder="Email"
                        type="text"
                        name="username"
                        className="border border-0 lz bg-transparent"
                        required
                        value={ loginData.email }
                        onChange={ handleChangeLoginData }
                    />
                    <div className="w-100 border border-bottom-1 border-light"></div>
                    <div className="invalid-feedback">Vui lòng nhập email hợp lệ.</div>
                </div>
                <div className="mb-3 form-input">
                    <input
                        placeholder="Password"
                        type="password"
                        name="password"
                        className="border border-0 text-light lz bg-transparent"
                        required
                        minLength={ 6 }
                        value={ loginData.password }
                        onChange={ handleChangeLoginData }
                    />
                    <div className="w-100 border border-bottom-1 border-light"></div>
                    <div className="invalid-feedback">Vui lòng nhập password</div>
                </div>
                <div className="d-flex justify-content-between register">
                    <p>Bạn chưa có tài khoản? <strong className="text-danger">Đăng ký</strong></p>
                    <Link className="mb-1 d-block">Quên mật khẩu</Link>
                </div>
                <button type="submit" className="btn btn-gardient w-100 mt-4 rounded-4 text-light">
                    { (isLoading) ? (
                        <div className="spinner-border text-light" role="status" style={ { height: '25px', width: '25px' } }>
                            <span className="visually-hidden">Loading...</span>
                        </div>
                    ) : "Đăng nhập" }
                </button>
            </form>
            <button className="btn btn-secondary w-100 mt-2 rounded-4"><i className="fa-brands fa-google"></i> Đăng nhập bằng google</button>
        </AuhenticationLayout>
    );
}

export default LoginPage;
