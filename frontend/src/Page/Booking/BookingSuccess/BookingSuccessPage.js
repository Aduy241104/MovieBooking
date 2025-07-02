// src/pages/BookingSuccessPage/BookingSuccessPage.jsx
import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { getBookingDetails } from '../../../service/BookingService'; // KIỂM TRA ĐƯỜNG DẪN
// import CustomizeButton from '../../../components/CustomeButton/CustomizeButton'; // ĐÃ XÓA
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';
import styles from './bookingSuccessPage.module.scss'; // SỬ DỤNG FILE SCSS MỚI
import classNames from 'classnames/bind';

const cx = classNames.bind(styles);

const BookingSuccessPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const queryParams = new URLSearchParams(location.search);

    // Ưu tiên lấy bookingId từ query params (khi redirect từ backend sau VNPAY)
    // Sau đó mới thử lấy từ location.state (khi navigate nội bộ từ BookingPage cho thanh toán tại quầy)
    const bookingIdFromQuery = queryParams.get('bookingId');
    const bookingIdFromState = location.state?.bookingId;
    const bookingId = bookingIdFromQuery || bookingIdFromState;

    console.log('BookingSuccessPage - Final bookingId to use:', bookingId);

    const [bookingDetails, setBookingDetails] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // --- LOGIC GỐC GIỮ NGUYÊN ---
    useEffect(() => {
        if (!bookingId) {
            console.warn('BookingSuccessPage: No bookingId found. Navigating to home.');
            navigate('/');
            return;
        }

        setLoading(true);
        setError('');
        const fetchDetails = async () => {
            try {
                const response = await getBookingDetails(Number(bookingId));
                if (response.data && response.data.result) {
                    setBookingDetails(response.data.result);
                } else {
                    setError("Không thể tải chi tiết đặt vé (dữ liệu không hợp lệ).");
                }
            } catch (err) {
                setError(err.response?.data?.message || "Lỗi khi tải chi tiết đặt vé.");
            } finally {
                setLoading(false);
            }
        };

        fetchDetails();
    }, [bookingId, navigate]);

    const formatScreeningDateTime = (dateTime) => {
        if (!dateTime) return 'N/A';
        try {
            return format(parseISO(dateTime), 'HH:mm - EEEE, dd/MM/yyyy', { locale: vi });
        } catch (e) {
            return 'N/A';
        }
    };

    const getStatusClass = (status) => {
        switch (status) {
            case 'PAID': return 'status-paid';
            case 'RESERVED': return 'status-reserved';
            case 'PENDING_PAYMENT': return 'status-pending';
            default: return 'status-unknown';
        }
    };

    // --- CÁC TRƯỜNG HỢP HIỂN THỊ ---
    if (loading) {
        return <div className={cx('page-container', 'centered-message')}>Đang tải thông tin đặt vé...</div>;
    }

    if (error) {
        return (
            <div className={cx('page-container')}>
                <div className={cx('content-wrapper', 'error-wrapper')}>
                    <i className={cx('icon-status', 'icon-error', 'fas fa-exclamation-triangle')}></i>
                    <h2 className={cx('title')}>Đã xảy ra lỗi</h2>
                    <p className={cx('message')}>{error}</p>
                    <div className={cx('actions-container')}>
                        <Link to="/">
                            <button className={cx('btn', 'btn-primary')}>Về Trang Chủ</button>
                        </Link>
                    </div>
                </div>
            </div>
        );
    }
    
    if (!bookingDetails) {
        return (
            <div className={cx('page-container')}>
                 <div className={cx('content-wrapper', 'error-wrapper')}>
                    <i className={cx('icon-status', 'icon-error', 'fas fa-question-circle')}></i>
                    <h2 className={cx('title')}>Thông tin không khả dụng</h2>
                    <p className={cx('message')}>Không tìm thấy chi tiết cho đơn đặt vé #{bookingId}. Vui lòng kiểm tra lại hoặc liên hệ hỗ trợ.</p>
                    <div className={cx('actions-container')}>
                         <Link to="/">
                            <button className={cx('btn', 'btn-primary')}>Về Trang Chủ</button>
                        </Link>
                    </div>
                </div>
            </div>
        );
    }

    // --- HIỂN THỊ KHI THÀNH CÔNG ---
    return (
        <div className={cx('page-container')}>
            <div className={cx('content-wrapper')}>
                <i className={cx('icon-status', 'icon-success', 'fas fa-check-circle')}></i>
                <h2 className={cx('title')}>Đặt vé thành công!</h2>
                <p className={cx('message')}>Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi.</p>
                <p className={cx('booking-code')}>
                    Mã đặt vé của bạn là: <strong>{bookingDetails.bookingCode}</strong>
                </p>

                <div className={cx('details-card')}>
                    <div className={cx('card-header')}>Chi tiết vé</div>
                    <div className={cx('card-body')}>
                        <div className={cx('info-item')}><span>Phim:</span><span>{bookingDetails.screening?.movieNameVn || 'N/A'}</span></div>
                        <div className={cx('info-item')}><span>Suất chiếu:</span><span>{formatScreeningDateTime(bookingDetails.screening?.showDateTime)}</span></div>
                        <div className={cx('info-item')}><span>Phòng chiếu:</span><span>{bookingDetails.screening?.cinemaRoomName || 'N/A'}</span></div>
                        <div className={cx('info-item')}><span>Ghế:</span><span className={cx('seats')}>{bookingDetails.bookedSeats?.map(s => `${s.seatRow}${s.seatCol}`).join(', ') || 'N/A'}</span></div>
                        <div className={cx('info-item')}><span>Tổng tiền:</span><span>{(bookingDetails.totalAmount || 0).toLocaleString('vi-VN')}đ</span></div>
                        {bookingDetails.paymentMethod?.methodName && <div className={cx('info-item')}><span>Thanh toán:</span><span>{bookingDetails.paymentMethod.methodName}</span></div>}
                        <div className={cx('info-item')}><span>Trạng thái:</span><span className={cx('status-badge', getStatusClass(bookingDetails.bookingStatus))}>{bookingDetails.bookingStatus || 'N/A'}</span></div>
                    </div>
                </div>

                <div className={cx('actions-container')}>
                    <Link to="/booking/history">
                        <button className={cx('btn', 'btn-secondary')}>Xem Lịch Sử Đặt Vé</button>
                    </Link>
                    <Link to="/">
                        <button className={cx('btn', 'btn-primary')}>Về Trang Chủ</button>
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default BookingSuccessPage;