import classNames from "classnames/bind";
import styles from './Header.module.scss';
import CustomizeText from "../../../components/CustomizeText";
import CustomizeButton from "../../../components/CustomeButton";
import { useState } from "react";
import Search from "../Search";

const cx = classNames.bind(styles);

function Header({ isLogin }) {
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    const toggleMenu = () => setIsMenuOpen(!isMenuOpen);
    const closeMenu = () => setIsMenuOpen(false);

    return (
        <header className={ `border-bottom border-dark bg-black ${cx("header")}` }>
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
                                <li className="border-bottom border-3 border-danger">Lịch chiếu</li>
                                <li className="border-bottom border-dark">Phim Chiếu</li>
                                <li>Sắp Chiếu</li>
                                <li>Top Phim</li>
                            </ul>
                        </nav>
                        <div className="d-flex align-items-center h-100">
                            <Search>
                                <button className="bg-transparent me-3 border-0">
                                    <i className="fa-solid fa-magnifying-glass text-light"></i>
                                </button>
                            </Search>
                            { (isLogin) ? ("") : (<CustomizeButton primary small>Đăng nhập</CustomizeButton>) }
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
