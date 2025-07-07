

function Footer() {
    return (
        <div className="container-fluid" style={ { backgroundColor: "#0F111A" } }>
            <div className="row pt-5 pb-5 mt-5 ms-md-5 px-3">

                <div className="col-12 col-md-6 text-light">
                    <p className="fs-1 fw-bold text-red">
                        <i className="fa-solid fa-circle-play"></i> GROUP 4
                    </p>

                    {/* Link Menu */ }
                    <ul className="p-0 d-flex flex-wrap justify-content-start list-unstyled">
                        <li className="p-2 ps-0 pe-3 fs-7 cursor-pointer">Hỏi-Đáp</li>
                        <li className="p-2 pe-3 fs-7 cursor-pointer">Giới thiệu</li>
                        <li className="p-2 pe-3 fs-7 cursor-pointer">Liên hệ</li>
                        <li className="p-2 pe-3 fs-7 cursor-pointer">Chính sách bảo mật</li>
                        <li className="p-2 pe-3 fs-7 cursor-pointer">Điều khoản sử dụng</li>
                    </ul>

                    {/* Social Icons */ }
                    <div className="pb-3 d-flex flex-wrap">
                        <button className="rounded-5 bg-dark me-2 mb-2" style={ { width: "40px", height: '40px' } }>
                            <i className="fa-brands fa-instagram"></i>
                        </button>
                        <button className="rounded-5 bg-dark me-2 mb-2" style={ { width: "40px", height: '40px' } }>
                            <i className="fa-brands fa-youtube"></i>
                        </button>
                        <button className="rounded-5 bg-dark me-2 mb-2" style={ { width: "40px", height: '40px' } }>
                            <i className="fa-brands fa-square-facebook"></i>
                        </button>
                        <button className="rounded-5 bg-dark me-2 mb-2" style={ { width: "40px", height: '40px' } }>
                            <i className="fa-brands fa-square-reddit"></i>
                        </button>
                    </div>

                    {/* Description */ }
                    <p className="fs-7 text-secondary">
                        RoPhim – Phim hay cả rổ - Trang xem phim online chất lượng cao miễn phí
                        Vietsub, thuyết minh, lồng tiếng full HD. Kho phim mới khổng lồ,
                        phim chiếu rạp, phim bộ, phim lẻ từ nhiều quốc gia như Việt Nam, Hàn Quốc,
                        Trung Quốc, Thái Lan, Nhật Bản, Âu Mỹ… đa dạng thể loại. Khám phá nền tảng phim
                        trực tuyến hay nhất 2024 chất lượng 4K!
                        <br /><br />
                        © 2024 RoPhim
                    </p>
                </div>
            </div>
        </div>
    
    )
}

export default Footer