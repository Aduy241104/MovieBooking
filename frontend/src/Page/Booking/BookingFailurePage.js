// src/pages/BookingFailurePage/BookingFailurePage.js
import React, { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import CustomizeButton from '../../components/CustomeButton/CustomizeButton'; // TODO: KIỂM TRA ĐƯỜNG DẪN

const BookingFailurePage = () => {
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const reason = queryParams.get('reason');
    const bookingIdFromQuery = queryParams.get('bookingId');
    const vnpResponseCode = queryParams.get('vnp_ResponseCode');

    const [retrievedMovieInfo, setRetrievedMovieInfo] = useState(null);
    const [tryAgainLink, setTryAgainLink] = useState('/');

    useEffect(() => {
        let movieInfoString = localStorage.getItem('lastMovieInfoForBooking');
        let movieIdForLink = null;

        if (movieInfoString) {
            try {
                const parsedMovieInfo = JSON.parse(movieInfoString);
                setRetrievedMovieInfo(parsedMovieInfo); // Lưu lại nếu cần hiển thị tên phim v.v.
                if (parsedMovieInfo && parsedMovieInfo.id) {
                    movieIdForLink = parsedMovieInfo.id;
                }
                // Xóa sau khi lấy để tránh dùng lại ở lần thất bại khác nếu người dùng không đi từ BookingPage
                localStorage.removeItem('lastMovieInfoForBooking');
            } catch (e) {
                console.error('BookingFailurePage - Error parsing movieInfo from localStorage:', e);
                // Nếu parse lỗi, thử lấy movieId dự phòng nếu có
                movieIdForLink = localStorage.getItem('lastMovieIdForBooking');
                if (movieIdForLink) {
                    localStorage.removeItem('lastMovieIdForBooking');
                }
            }
        } else {
            // Thử lấy movieId dự phòng nếu không có movieInfo
            movieIdForLink = localStorage.getItem('lastMovieIdForBooking');
            if (movieIdForLink) {
                localStorage.removeItem('lastMovieIdForBooking');
            }
        }


        if (movieIdForLink) {
            setTryAgainLink(`/movie/${movieIdForLink}`);
        } else {
            setTryAgainLink('/'); // Mặc định về trang chủ nếu không có thông tin phim
        }

        // Xóa bookingId nếu bạn đã lưu (tùy chọn)
        // localStorage.removeItem('lastVnpayBookingId');

    }, []); // Chạy một lần khi component mount


    let message = "Giao dịch không thành công.";
    if (reason === 'payment_declined') {
        message = `Thanh toán bị từ chối bởi VNPAY. (Mã lỗi: ${vnpResponseCode || 'N/A'})`;
    } else if (reason === 'invalid_signature') {
        message = "Xác thực thanh toán không thành công. Vui lòng liên hệ bộ phận hỗ trợ.";
    } else if (reason === 'booking_not_found') {
        message = "Không tìm thấy thông tin đặt vé liên quan. Vui lòng thử lại từ đầu.";
    } else if (reason === 'booking_already_processed') {
        message = "Giao dịch cho đặt vé này đã được xử lý trước đó.";
    } else if (reason) {
        message = `Đã xảy ra lỗi: ${reason}.`;
    }


    return (
        <div className="container mt-5 text-center">
            <i className="fas fa-times-circle fa-5x text-danger mb-3"></i>
            <h2>Đặt vé không thành công!</h2>
            {retrievedMovieInfo && <p className="text-muted">Phim: {retrievedMovieInfo.nameVN || retrievedMovieInfo.nameEN}</p>}
            <p>{message}</p>
            {bookingIdFromQuery && <p>Mã tham chiếu (nếu có): #{bookingIdFromQuery}</p>}
            <p>Vui lòng thử lại hoặc chọn phương thức thanh toán khác.</p>
            <div className="mt-4 d-flex justify-content-center gap-3">
                <Link to={tryAgainLink}>
                    <CustomizeButton secondary large>
                        {tryAgainLink === '/' ? 'Về Trang Chủ' : 'Chọn Lại Suất Chiếu'}
                    </CustomizeButton>
                </Link>
                {tryAgainLink !== '/' && ( // Chỉ hiển thị nút "Về Trang Chủ" thứ hai nếu nút "Thử Lại" không phải là về trang chủ
                     <Link to="/">
                        <CustomizeButton primary large>
                            Về Trang Chủ
                        </CustomizeButton>
                    </Link>
                )}
            </div>
        </div>
    );
};

export default BookingFailurePage;