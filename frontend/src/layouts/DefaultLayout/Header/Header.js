import classNames from "classnames/bind";
import styles from './Header.module.scss';
import CustomizeText from "../../../components/CustomizeText";
import CustomizeButton from "../../../components/CustomeButton";
import { useState, useEffect } from "react";
import Search from "../Search";
import Avatar from "../../../components/Avatar/Avatar";
import { Dropdown } from 'antd';
import { useNavigate } from 'react-router-dom';
import menu from "./MenuItem/menu";
import { NotificationBell } from "../../../components/Notification/NotificationBell";


const cx = classNames.bind(styles);

function Header({ user }) {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const toggleMenu = () => setIsMenuOpen(!isMenuOpen);
    const closeMenu = () => setIsMenuOpen(false);
    const [isScrolled, setIsScrolled] = useState(false);
    const navigate = useNavigate();

    const items = menu();

    useEffect(() => {
        const handleScroll = () => {
            if (window.scrollY > 70) {
                setIsScrolled(true);
            } else {
                setIsScrolled(false);
            }
        };

        window.addEventListener("scroll", handleScroll);
        return () => window.removeEventListener("scroll", handleScroll);
    }, []);

    return (
        <header className={ cx("header", { "scrolled": isScrolled }) }>
            <div className="container-fluid px-3 h-100">
                <div className="d-flex justify-content-between align-items-center py-2 h-100">
                    {/* Logo */ }
                    <div className="text-light ms-5">
                        <CustomizeText level={ 'h4' }>G4</CustomizeText>
                    </div>

                    {/* Menu Toggle - Mobile only */ }
                    <button className="d-lg-none bg-transparent border-0 text-white" onClick={ toggleMenu }>
                        <i className="fa-solid fa-bars fa-xl"></i>
                    </button>


                    {/* Navigation & Actions - Desktop */ }
                    <div className="me-5 d-none d-lg-flex justify-content-center align-items-center flex-grow-1">
                        <nav className="d-flex justify-content-end me-5 flex-fill ms-5">
                            <ul className="d-flex justify-content-evenly align-items-center text-light fw-bold list-unstyled mb-0 gap-4">
                                <li className="" onClick={ () => navigate('/') }>Trang chủ</li>
                                <li className="">Lịch chiếu</li>
                                <li className="">Sắp Chiếu</li>
                                <li className="">Top Phim</li>
                            </ul>
                        </nav>
                        <div className="d-flex align-items-center h-100">
                            <Search />

                            { user ? (
                                <>
                                    <div className="d-flex align-items-center gap-1">
                                        <NotificationBell accountId={ user?.accountId } />
                                        <Dropdown menu={ { items } } trigger={ ['click'] } placement="bottomRight">
                                            <span className="d-flex" style={ { cursor: 'pointer', marginLeft: 16 } }>
                                                <Avatar
                                                    src={ user.avatar + "" }
                                                    fallBack="/Assests/Image/Screenshot 2025-06-13 102311.png"

                                                />
                                                <i className="fa-solid fa-sort-down ms-2 fs-8 mt-2 text-light"></i>
                                            </span>
                                        </Dropdown>
                                    </div>
                                </>
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
