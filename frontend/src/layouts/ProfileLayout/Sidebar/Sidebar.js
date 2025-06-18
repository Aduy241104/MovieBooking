import { NavLink, useNavigate } from "react-router-dom";
import classNames from "classnames/bind";
import styles from './Sidebar.module.scss'
import { AuthContext } from "../../../context/AuthContext";
import { useContext } from "react";
import Avatar from "../../../components/Avatar/Avatar";

const cx = classNames.bind(styles);

function Sidebar() {
    const { user } = useContext(AuthContext);
    const navigate = useNavigate();


    return (
        <div className={ cx("p-3 ms-5", 'sidebar') }>
            <div className={ cx('p-5 rounded-4', 'inner-sidebar') }>
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
                            <i className="fa-solid fa-user"></i> <span className="ps-2 fs-7">Tài khoản</span>
                        </NavLink>
                    </li>
                    <li className={ cx('mb-4', 'link-layout') }>
                        <NavLink
                            to="/profile/password"
                            className={ ({ isActive }) =>
                                cx(isActive ? "text-red fw-bolder" : "text-light", "btn-layout")
                            }
                        >
                            <i className="fa-solid fa-key"></i> <span className="ps-2 fs-7">Mật khẩu</span>
                        </NavLink>
                    </li>
                    <li className={ cx('mb-4', 'link-layout') }>
                        <NavLink
                            to="/profile/transactions"
                            className={ ({ isActive }) =>
                                cx(isActive ? "text-red fw-bolder" : "text-light", "btn-layout")
                            }
                        >
                            <i className="fa-solid fa-clock-rotate-left"></i> <span className="ps-2 fs-7">Giao dịch</span>
                        </NavLink>
                    </li>
                </ul>

                {/* User info at bottom */ }
                <div className="mb-5 mt-5 pt-5">
                    <div className={ cx("rounded-full mb-2", 'avt') }>
                        { user &&
                            <Avatar
                                className={ cx('avt') }
                                src={ user.avatar + "" }
                                fallBack={ '/Assests/Image/Screenshot 2025-06-13 102311.png' }
                            />
                        }
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