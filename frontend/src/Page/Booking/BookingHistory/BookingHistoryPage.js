import React, { useEffect, useState, useContext } from 'react';
import { Link } from 'react-router-dom';
import { getUserBookingHistory, retryPayment } from '../../../service/BookingService'; // Đảm bảo đường dẫn đúng
import { AuthContext } from '../../../context/AuthContext'; // Đảm bảo đường dẫn đúng
import { format, parseISO, addMinutes, isAfter } from 'date-fns';
import { vi } from 'date-fns/locale';
import styles from './bookingHistoryPage.module.scss'; // Đảm bảo có file scss tương ứng
import classNames from 'classnames/bind';
import CountdownTimer from '../../../components/Booking/CountdownTimer'; // Đảm bảo đường dẫn đúng

const cx = classNames.bind(styles);

const BookingHistoryPage = () => {
    const { user } = useContext(AuthContext);
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [retryingPaymentId, setRetryingPaymentId] = useState(null);

    // Hàm để tải lại lịch sử, có thể gọi lại khi cần
    const fetchHistory = async () => {
        setLoading(true);
        setError('');
        try {
            const response = await getUserBookingHistory();
            if (response.data && response.data.result) {
                setBookings(response.data.result);
            } else {
                setBookings([]);
            }
        } catch (err) {
            setError(err.response?.data?.message || "Không thể tải lịch sử đặt vé.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (user) {
            fetchHistory();
        }
    }, [user]);

    const handleRetryPayment = async (e, bookingId) => {
        e.preventDefault(); // Ngăn thẻ <Link> điều hướng ngay lập tức
        e.stopPropagation(); // Ngăn sự kiện click nổi bọt lên thẻ <Link>

        setRetryingPaymentId(bookingId);
        try {
            const response = await retryPayment(bookingId);
            if (response.data?.result) {
                // Nếu thành công, chuyển hướng người dùng đến cổng thanh toán
                window.location.href = response.data.result;
            } else {
                // Nếu API trả về thành công nhưng không có URL
                alert("Không thể tạo lại link thanh toán. Vé có thể đã hết hạn.");
                fetchHistory(); // Tải lại lịch sử để cập nhật trạng thái
            }
        } catch (error) {
            const errorMessage = error.response?.data?.message;
            if (errorMessage === 'BOOKING_EXPIRED') {
                alert("Vé đã hết hạn thanh toán. Trang sẽ được tải lại.");
            } else if (errorMessage === 'BOOKING_NOT_PENDING') {
                alert("Vé này không còn ở trạng thái chờ thanh toán. Trang sẽ được tải lại.");
            } else {
                alert("Lỗi khi thử thanh toán lại. Vui lòng thử lại sau.");
            }
            fetchHistory(); // Tải lại trong mọi trường hợp lỗi để đồng bộ
        } finally {
            setRetryingPaymentId(null);
        }
    };

    const formatScreeningDateTime = (dateTime) => {
        if (!dateTime) return 'N/A';
        try {
            return format(parseISO(dateTime), 'HH:mm - EEEE, dd/MM/yyyy', { locale: vi });
        } catch (e) {
            return 'N/A';
        }
    };
    
    // Ánh xạ trạng thái sang class CSS để dễ style
    const getStatusInfo = (status, isExpired) => {
        if (status === 'PENDING_PAYMENT' && isExpired) {
            return { text: 'Đã hết hạn', className: 'status-expired' };
        }
        switch (status) {
            case 'PAID': return { text: 'Đã thanh toán', className: 'status-paid' };
            case 'RESERVED': return { text: 'Đã giữ chỗ', className: 'status-reserved' };
            case 'PENDING_PAYMENT': return { text: 'Chờ thanh toán', className: 'status-pending' };
            case 'CANCELLED': return { text: 'Đã hủy', className: 'status-cancelled' };
            case 'EXPIRED': return { text: 'Đã hết hạn', className: 'status-expired' };
            case 'PAYMENT_FAILED': return { text: 'Thanh toán thất bại', className: 'status-failed' };
            default: return { text: status || 'Không xác định', className: 'status-unknown' };
        }
    };

    const renderBookingItem = (booking) => {
        const isPending = booking.bookingStatus === 'PENDING_PAYMENT';
        const expiryTime = addMinutes(parseISO(booking.bookingTime), 15); // Thời gian hết hạn là 15 phút sau khi đặt
        const isExpired = isAfter(new Date(), expiryTime);

        const statusInfo = getStatusInfo(booking.bookingStatus, isExpired);

        return (
            <Link to={`/profile/booking-details/${booking.bookingId}`} key={booking.bookingId} className={cx('booking-item')}>
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
                    <div className={cx('status-badge', statusInfo.className)}>
                        {statusInfo.text}
                    </div>
                </div>

                {/* Khu vực hành động chỉ hiển thị cho các booking đang chờ và chưa hết hạn */}
                {isPending && !isExpired && (
                    <div className={cx('pending-actions')}>
                        <CountdownTimer 
                            expiryTime={expiryTime}
                            onExpire={() => fetchHistory()} // Tự động tải lại danh sách khi đồng hồ hết giờ
                        />
                        <button
                            onClick={(e) => handleRetryPayment(e, booking.bookingId)}
                            disabled={retryingPaymentId === booking.bookingId}
                            className={cx('btn-retry-payment')}
                        >
                            {retryingPaymentId === booking.bookingId ? 'Đang xử lý...' : 'Thanh toán ngay'}
                        </button>
                    </div>
                )}
            </Link>
        );
    };

    const renderContent = () => {
        if (loading) {
            return <div className={cx('centered-message')}>Đang tải lịch sử đặt vé...</div>;
        }
        if (error) {
            return <div className={cx('centered-message', 'error-message')}>{error}</div>;
        }
        if (bookings.length === 0) {
            return <div className={cx('centered-message')}>Bạn chưa có đơn đặt vé nào.</div>;
        }
        
        return (
            <div className={cx('booking-list')}>
                {bookings.map(booking => renderBookingItem(booking))}
            </div>
        );
    };

    return (
        <div className={cx('history-page-wrapper')}>
            <h2 className={cx('page-title')}>Lịch sử đặt vé</h2>
            {renderContent()}
        </div>
    );
};

export default BookingHistoryPage;