import React, { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import styles from './bookingFailurePage.module.scss'; 
import classNames from 'classnames/bind';
import DefaultLayout from '../../../layouts/DefaultLayout';

const cx = classNames.bind(styles);

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
                setRetrievedMovieInfo(parsedMovieInfo); 
                if (parsedMovieInfo && parsedMovieInfo.id) {
                    movieIdForLink = parsedMovieInfo.id;
                }
                localStorage.removeItem('lastMovieInfoForBooking');
            } catch (e) {
                console.error('BookingFailurePage - Error parsing movieInfo from localStorage:', e);
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
            setTryAgainLink('/'); 
        }

    }, []); // Run once when component mounts

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
        <DefaultLayout>
        <div className={cx('page-container')}>
            <div className={cx('content-wrapper')}>
                <i className={cx('icon-failure', 'fas fa-times-circle')}></i>
                <h2 className={cx('title')}>Đặt vé không thành công!</h2>

                {retrievedMovieInfo && <p className={cx('movie-info')}>Phim: {retrievedMovieInfo.nameVN || retrievedMovieInfo.nameEN}</p>}
                
                <p className={cx('message')}>{message}</p>
                
                {bookingIdFromQuery && <p className={cx('reference-code')}>Mã tham chiếu: #{bookingIdFromQuery}</p>}
                
                <p className={cx('sub-message')}>Vui lòng thử lại hoặc chọn phương thức thanh toán khác.</p>
                
                <div className={cx('actions-container')}>
                    {/* Thay thế CustomizeButton bằng Link và button thông thường */}
                    <Link to={tryAgainLink}>
                        <button className={cx('btn', 'btn-secondary')}>
                            {tryAgainLink === '/' ? 'Về Trang Chủ' : 'Chọn Lại Suất Chiếu'}
                        </button>
                    </Link>
                    
                    {tryAgainLink !== '/' && ( 
                         <Link to="/">
                            <button className={cx('btn', 'btn-primary')}>
                                Về Trang Chủ
                            </button>
                        </Link>
                    )}
                </div>
            </div>
        </div>
         </DefaultLayout>
    );
};

export default BookingFailurePage;