// src/pages/BookingHistoryPage/BookingHistoryPage.jsx

import React, { useEffect, useState, useContext } from 'react';
import { Link } from 'react-router-dom';
import { getUserBookingHistory } from '../../../service/BookingService';
import { AuthContext } from '../../../context/AuthContext';
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';
import styles from './bookingHistoryPage.module.scss';
import classNames from 'classnames/bind';

// KHÔNG CẦN import DefaultLayout hay ProfileLayout/Sidebar ở đây nữa
// import DefaultLayout from '../../../layouts/DefaultLayout';
// import Sidebar from '../../../layouts/ProfileLayout/Sidebar';

const cx = classNames.bind(styles);

const BookingHistoryPage = () => {
    // --- Toàn bộ logic, state, useEffect, các hàm helper của bạn được giữ nguyên ---
    const { user } = useContext(AuthContext);
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    
    useEffect(() => {
        if (user) {
            const fetchHistory = async () => {
                setLoading(true);
                setError('');
                try {
                    const response = await getUserBookingHistory();
                    if (response.data && response.data.result) {
                        setBookings(response.data.result);
                    }
                } catch (err) {
                    setError(err.response?.data?.message || "Không thể tải lịch sử đặt vé.");
                } finally {
                    setLoading(false);
                }
            };
            fetchHistory();
        }
    }, [user]);

    const formatScreeningDateTime = (dateTime) => {
        if (!dateTime) return 'N/A';
        try {
            return format(parseISO(dateTime), 'HH:mm - EEEE, dd/MM/yyyy', { locale: vi });
        } catch (e) { return 'N/A'; }
    };

    const getStatusClass = (status) => {
        switch (status) {
            case 'PAID': return 'status-paid';
            case 'RESERVED': return 'status-reserved';
            case 'PENDING_PAYMENT': return 'status-pending';
            case 'CANCELLED': return 'status-cancelled';
            case 'PAYMENT_FAILED': return 'status-failed';
            default: return 'status-unknown';
        }
    };

    const renderContent = () => {
        if (loading) return <div className={cx('centered-message')}>Đang tải lịch sử đặt vé...</div>;
        if (error) return <div className={cx('centered-message', 'error-message')}>{error}</div>;
        if (bookings.length === 0) return <div className={cx('centered-message')}>Bạn chưa có đơn đặt vé nào.</div>;
        
        return (
            <div className={cx('booking-list')}>
                {bookings.map(booking => (
                    <Link
                        to={`/booking/details/${booking.bookingId}`} // Link này vẫn giữ nguyên
                        key={booking.bookingId}
                        className={cx('booking-item')}
                    >
                        {/* Toàn bộ nội dung item giữ nguyên */}
                        <div className={cx('item-header')}>
                           <h5 className={cx('movie-title')}>{booking.screening?.movieNameVn || 'N/A'}</h5>
                           <span className={cx('booking-code')}>Mã vé: <strong>{booking.bookingCode}</strong></span>
                        </div>
                        <div className={cx('item-body')}>
                            <div className={cx('info-row')}>
                                <span className={cx('info-label')}><i className="fas fa-calendar-check"></i> Đặt lúc:</span>
                                <span className={cx('info-value')}>{formatScreeningDateTime(booking.bookingTime)}</span>
                            </div>
                            <div className={cx('info-row')}>
                                <span className={cx('info-label')}><i className="fas fa-clock"></i> Suất chiếu:</span>
                                <span className={cx('info-value')}>{`${formatScreeningDateTime(booking.screening?.showDateTime)} - ${booking.screening?.cinemaRoomName}`}</span>
                            </div>
                            <div className={cx('info-row')}>
                                <span className={cx('info-label')}><i className="fas fa-chair"></i> Ghế:</span>
                                <span className={cx('info-value', 'seats')}>{booking.bookedSeats?.map(s => `${s.seatRow}${s.seatCol}`).join(', ') || 'N/A'}</span>
                            </div>
                        </div>
                        <div className={cx('item-footer')}>
                            <div className={cx('total-amount')}>
                                Tổng cộng: <strong>{booking.totalAmount?.toLocaleString('vi-VN')}đ</strong>
                                {booking.promotionCodeApplied && <span> (KM: {booking.promotionCodeApplied})</span>}
                            </div>
                            <div className={cx('status-badge', getStatusClass(booking.bookingStatus))}>
                                {booking.bookingStatus}
                            </div>
                        </div>
                    </Link>
                ))}
            </div>
        );
    };

    // Component chỉ cần return phần nội dung của chính nó
    return (
        <div className={cx('history-page-wrapper')}>
            <h2 className={cx('page-title')}>Lịch sử đặt vé</h2>
            {renderContent()}
        </div>
    );
};

export default BookingHistoryPage;