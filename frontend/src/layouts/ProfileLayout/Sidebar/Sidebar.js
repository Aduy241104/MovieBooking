import { NavLink, useNavigate } from "react-router-dom";
import classNames from "classnames/bind";
import styles from './Sidebar.module.scss'

const cx = classNames.bind(styles);

function Sidebar() {
    const navigate = useNavigate();


    return (
        <div className={ cx("p-3", 'sidebar') }>
            <div className={ cx('p-5 rounded-4', 'inner-sidebar')}>
                <div className="mb-3">
                    <button
                        className="red-hover"
                        onClick={ () => navigate("/") }
                    >
                        <i className="fa-solid fa-arrow-left"></i> Trở về
                    </button>
                </div>
                <h5 className="fw-bolder">Quản lý tài khoản</h5>
                <ul className="list-unstyled mt-5 pb-5">
                    <li className={ cx('mb-4', 'link-layout') }>
                        <NavLink
                            to="/profile"
                            end
                            className={ ({ isActive }) =>
                                cx(isActive ? "text-red fw-bolder" : "text-light", "btn-layout")
                            }
                        >
                            <i className="fa-solid fa-user"></i> <span className="ps-2">Tài khoản</span>
                        </NavLink>
                    </li>
                    <li className={ cx('mb-4', 'link-layout') }>
                        <NavLink
                            to="/profile/password"
                            className={ ({ isActive }) =>
                                cx(isActive ? "text-red fw-bolder" : "text-light", "btn-layout")
                            }
                        >
                            <i className="fa-solid fa-key"></i> <span className="ps-2">Mật khẩu</span>
                        </NavLink>
                    </li>
                    <li className={ cx('mb-4', 'link-layout') }>
                        <NavLink
                            to="/profile/transactions"
                            className={ ({ isActive }) =>
                                cx(isActive ? "text-red fw-bolder" : "text-light", "btn-layout")
                            }
                        >
                            <i className="fa-solid fa-clock-rotate-left"></i> <span className="ps-2">Giao dịch</span>
                        </NavLink>
                    </li>
                </ul>

                {/* User info at bottom */ }
                <div className="mb-5 mt-5 pt-5">
                    <div className={ cx("rounded-full mb-2", 'avt') }>
                        <img src="https://p16-sign-va.tiktokcdn.com/tos-maliva-avt-0068/a0e63af2063dccd1389e1bc27ee465ba~tplv-tiktokx-cropcenter:1080:1080.jpeg?dr=14579&refresh_token=76b54e80&x-expires=1749092400&x-signature=L%2FIqvwELh%2BmxK9fobJMEfORbNys%3D&t=4d5b0474&ps=13740610&shp=a5d48078&shcp=81f88b70&idc=my"
                            alt="avatar"
                        />
                    </div>
                    <p>music app</p>
                    <p className="text-secondary fs-7">duya15914@gmail.com</p>

                    <button className="mt-3 fs-6">
                        <i className="fa-solid fa-arrow-right-from-bracket pe-2"></i>
                        Đăng xuất
                    </button>
                </div>
            </div>
        </div >
    )
}

export default Sidebar