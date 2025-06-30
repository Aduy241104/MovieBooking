import React, { useEffect, useState, useContext } from 'react';
import { Link } from 'react-router-dom';
import { getUserBookingHistory } from '../../service/BookingService';
import { AuthContext } from '../../context/AuthContext';
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';
// import styles from './bookingHistoryPage.module.scss'; // Tạo file css nếu cần
// import classNames from 'classnames/bind';

// const cx = classNames.bind(styles);

const BookingHistoryPage = () => {
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
                    console.error("Error fetching booking history:", err);
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
            return format(parseISO(dateTime), 'HH:mm dd/MM/yyyy', { locale: vi });
        } catch (e) { return 'N/A'; }
    };

    if (loading) {
        return <div className="container mt-5"><p>Đang tải lịch sử đặt vé...</p></div>;
    }

    if (error) {
        return <div className="container mt-5"><p className="text-danger">{error}</p></div>;
    }

    return (
        <div className="container mt-4 mb-5">
            <h2>Lịch sử đặt vé</h2>
            {bookings.length === 0 ? (
                <p>Bạn chưa có đơn đặt vé nào.</p>
            ) : (
                <div className="list-group">
                    {bookings.map(booking => (
                        <Link
                            to={`/booking/details/${booking.bookingId}`} // Link tới trang chi tiết vé nếu có
                            key={booking.bookingId}
                            className="list-group-item list-group-item-action flex-column align-items-start mb-3 shadow-sm"
                        >
                            <div className="d-flex w-100 justify-content-between">
                                <h5 className="mb-1">{booking.screening?.movieNameVn || 'N/A'}</h5>
                               <small>Mã vé: <strong>{booking.bookingCode}</strong></small>
                            </div>
                            <p className="mb-1">
                                Đặt lúc: {formatScreeningDateTime(booking.bookingTime)}
                            </p>
                            <p className="mb-1">
                                Suất chiếu: {formatScreeningDateTime(booking.screening?.showDateTime)} - Phòng: {booking.screening?.cinemaRoomName}
                            </p>
                            <p className="mb-1">
                                Ghế: {booking.bookedSeats?.map(s => `${s.seatRow}${s.seatCol}`).join(', ') || 'N/A'}
                            </p>
                            <p className="mb-1">
                                Tổng tiền: {booking.totalAmount?.toLocaleString('vi-VN')}đ
                                {booking.promotionCodeApplied && ` (KM: ${booking.promotionCodeApplied})`}
                            </p>
                            <small>Trạng thái: <span className={`badge bg-${
                                booking.bookingStatus === 'PAID' ? 'success' :
                                booking.bookingStatus === 'RESERVED' ? 'info' :
                                booking.bookingStatus === 'PENDING_PAYMENT' ? 'warning' :
                                'danger'
                            }`}>{booking.bookingStatus}</span></small>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
};

export default BookingHistoryPage;