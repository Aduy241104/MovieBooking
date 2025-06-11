import classNames from "classnames/bind";
import styles from './Header.module.scss';
import CustomizeText from "../../../components/CustomizeText";
import CustomizeButton from "../../../components/CustomeButton";
import { useState } from "react";
import Search from "../Search";
import Avatar from "../../../components/Avatar/Avatar";
import { Dropdown } from 'antd';
import { useNavigate } from 'react-router-dom';

const cx = classNames.bind(styles);

function Header({ user, logout }) {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const toggleMenu = () => setIsMenuOpen(!isMenuOpen);
    const closeMenu = () => setIsMenuOpen(false);
    const navigate = useNavigate();

    const items = [
        {
            key: 'welcome',
            label: (
                <p
                    style={ {
                        background: 'none',
                        border: 'none',
                        width: '100%',
                        textAlign: 'left',
                        padding: '6px 12px',
                        width:'160px'
                    } }
                    onClick={ () => navigate('/profile') }
                    className="text-secondary border-bottom border-gray"
                >
                    Chào <br />
                    Anh Duy 😚
                </p>
            ),
        },
        {
            key: 'profile',
            label: (
                <button
                    style={ {
                        background: 'none',
                        border: 'none',
                        width: '100%',
                        textAlign: 'left',
                        padding: '6px 12px',
                    } }
                    onClick={ () => navigate('/profile') }
                    className="fw-bold"
                >
                    <i className="fa-solid fa-user me-2"></i>
                    Tài khoản
                </button>
            ),
        },
        {
            key: 'logout',
            label: (
                <button
                    style={ {
                        background: 'none',
                        border: 'none',
                        width: '100%',
                        textAlign: 'left',
                        padding: '6px 12px',
                        color: 'red',
                    } }
                    onClick={ logout }
                    className="fw-bold"
                >
                    <i className="fa-solid fa-arrow-right-from-bracket me-2"></i>
                    Đăng xuất
                </button>
            ),
        },
    ];

    return (
        <header className={ `border-bottom border-dark ${cx("header")}` }>
            <div className="container px-3 h-100">
                <div className="d-flex justify-content-between align-items-center py-2 h-100">
                    {/* Logo */ }
                    <div className="text-light">
                        <CustomizeText level={ 'h4' }>GROUP 4</CustomizeText>
                    </div>

                    {/* Menu Toggle - Mobile only */ }
                    <button className="d-lg-none bg-transparent border-0 text-white" onClick={ toggleMenu }>
                        <i className="fa-solid fa-bars fa-xl"></i>
                    </button>


                    {/* Navigation & Actions - Desktop */ }
                    <div className="d-none d-lg-flex justify-content-center align-items-center flex-grow-1">
                        <nav className="d-flex justify-content-end me-5 flex-fill ms-5">
                            <ul className="d-flex justify-content-evenly align-items-center text-light fw-bold list-unstyled mb-0 gap-4">
                                <li className="border-bottom border-gold ">Lịch chiếu</li>
                                <li className="border-bottom border-dark">Phim Chiếu</li>
                                <li className="border-bottom border-dark">Sắp Chiếu</li>
                                <li className="border-bottom border-dark">Top Phim</li>
                            </ul>
                        </nav>
                        <div className="d-flex align-items-center h-100">
                            <Search />

                            { user ? (
                                <Dropdown menu={ { items } } trigger={ ['click'] } placement="bottomRight">
                                    <span style={ { cursor: 'pointer', marginLeft: 10 } }>
                                        <Avatar src="https://p16-sign-va.tiktokcdn.com/tos-maliva-avt-0068/a0e63af2063dccd1389e1bc27ee465ba~tplv-tiktokx-cropcenter:1080:1080.jpeg?dr=14579&refresh_token=76b54e80&x-expires=1749092400&x-signature=L%2FIqvwELh%2BmxK9fobJMEfORbNys%3D&t=4d5b0474&ps=13740610&shp=a5d48078&shcp=81f88b70&idc=my" />
                                    </span>
                                </Dropdown>
                            ) : (
                                <CustomizeButton to="/login" primary small>
                                    Đăng nhập
                                </CustomizeButton>
                            ) }
                        </div>
                    </div>
                </div>
            </div>

            {/* Mobile Menu */ }
            { isMenuOpen && (
                <div className={ cx("mobileMenu") }>
                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <CustomizeText level={ 'h4' }>MENU</CustomizeText>
                        <button className="bg-transparent border-0 text-white" onClick={ closeMenu }>
                            <i className="fa-solid fa-xmark fa-xl"></i>
                        </button>
                    </div>
                    <ul className="list-unstyled text-light fw-bold mb-4">
                        <li className="py-2 border-bottom border-dark">Lịch chiếu</li>
                        <li className="py-2 border-bottom border-dark">Phim Chiếu</li>
                        <li className="py-2 border-bottom border-dark">Sắp Chiếu</li>
                        <li className="py-2 border-bottom border-dark">Top Phim</li>
                    </ul>
                    <div className="d-flex align-items-center gap-3">
                        <i className="fa-solid fa-magnifying-glass text-light"></i>
                        <CustomizeButton primary small>Đăng nhập</CustomizeButton>
                    </div>
                </div>
            ) }
        </header>
    );
}

export default Header;
