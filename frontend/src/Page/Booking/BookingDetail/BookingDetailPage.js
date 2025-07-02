import React, { useEffect, useState, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import { getBookingDetails } from '../../../service/BookingService'; // Điều chỉnh đường dẫn
import { AuthContext } from '../../../context/AuthContext'; // Điều chỉnh đường dẫn
// import CustomizeButton from '../../../components/CustomeButton/CustomizeButton'; // ĐÃ XÓA, không còn sử dụng
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';
import styles from './bookingDetailPage.module.scss'; // ĐÃ CẬP NHẬT để sử dụng file SCSS mới
import classNames from 'classnames/bind';

const cx = classNames.bind(styles);

const BookingDetailPage = () => {
    const { bookingId } = useParams();
    const navigate = useNavigate();
    const { user } = useContext(AuthContext);
    
    const [bookingDetails, setBookingDetails] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // --- LOGIC GỐC GIỮ NGUYÊN ---
    useEffect(() => {
        if (!bookingId || !user) {
            navigate('/login');
            return;
        }
        const fetchDetails = async () => {
            setLoading(true);
            setError('');
            try {
                const response = await getBookingDetails(bookingId);
                if (response.data && response.data.result) {
                    setBookingDetails(response.data.result);
                } else {
                    setError('Không tìm thấy thông tin đặt vé.');
                }
            } catch (err) {
                setError(err.response?.data?.message || "Lỗi khi tải chi tiết vé.");
            } finally {
                setLoading(false);
            }
        };
        fetchDetails();
    }, [bookingId, user, navigate]);

    const formatScreeningDateTime = (dateTime) => {
        if (!dateTime) return 'N/A';
        return format(parseISO(dateTime), 'HH:mm - EEEE, dd/MM/yyyy', { locale: vi });
    };

    const getStatusInfo = (status) => {
        switch (status) {
            case 'PAID': return { text: 'Đã thanh toán', className: 'status-paid' };
            case 'RESERVED': return { text: 'Đã giữ chỗ', className: 'status-reserved' };
            case 'PENDING_PAYMENT': return { text: 'Chờ thanh toán', className: 'status-pending' };
            case 'CANCELLED': return { text: 'Đã hủy', className: 'status-cancelled' };
            case 'PAYMENT_FAILED': return { text: 'Thanh toán thất bại', className: 'status-failed' };
            default: return { text: status || 'Không xác định', className: 'status-unknown' };
        }
    };
    
    if (loading) {
        return <div className={cx('page-container', 'loading')}>Đang tải chi tiết vé...</div>;
    }
    if (error) {
        return <div className={cx('page-container', 'error')}>{error}</div>;
    }
    if (!bookingDetails) {
        return <div className={cx('page-container', 'error')}>Không có dữ liệu để hiển thị.</div>;
    }
    
    // --- LOGIC GỐC GIỮ NGUYÊN ---
    const statusInfo = getStatusInfo(bookingDetails.bookingStatus);
    const qrValue = JSON.stringify({
        bookingCode: bookingDetails.bookingCode,
        movieName: bookingDetails.screening?.movieNameVn,
        showTime: bookingDetails.screening?.showDateTime,
    });
    const hasDiscount = (bookingDetails.discountApplied > 0) || (bookingDetails.pointsDiscount > 0);

    return (
        <div className={cx('page-container')}>
            <div className={cx('ticket-wrapper')}>
                {/* --- HEADER CỦA VÉ --- */}
                <div className={cx('ticket-header')}>
                    <h2 className={cx('title')}>Vé Xem Phim Điện Tử</h2>
                    <div className={cx('booking-code')}>Mã đặt vé: <strong>{bookingDetails.bookingCode}</strong></div>
                </div>

                {/* --- THÂN VÉ --- */}
                <div className={cx('ticket-body')}>
                    {/* --- CỘT TRÁI - THÔNG TIN CHI TIẾT --- */}
                    <div className={cx('left-panel')}>
                        <h4 className={cx('movie-title')}>{bookingDetails.screening?.movieNameVn}</h4>
                        <div className={cx('info-grid')}>
                            <div className={cx('info-item')}>
                                <span className={cx('label')}><i className="fas fa-user"></i> Khách hàng</span>
                                <span className={cx('value')}>{bookingDetails.account?.fullName}</span>
                            </div>
                            <div className={cx('info-item')}>
                                <span className={cx('label')}><i className="fas fa-envelope"></i> Email</span>
                                <span className={cx('value')}>{bookingDetails.account?.email}</span>
                            </div>
                            <div className={cx('info-item')}>
                                <span className={cx('label')}><i className="fas fa-calendar-alt"></i> Suất chiếu</span>
                                <span className={cx('value')}>{formatScreeningDateTime(bookingDetails.screening?.showDateTime)}</span>
                            </div>
                            <div className={cx('info-item')}>
                                <span className={cx('label')}><i className="fas fa-video"></i> Rạp / Định dạng</span>
                                <span className={cx('value')}>{`${bookingDetails.screening?.cinemaRoomName} / ${bookingDetails.screening?.movieFormat}`}</span>
                            </div>
                            <div className={cx('info-item', 'full-width')}>
                                <span className={cx('label')}><i className="fas fa-chair"></i> Ghế đã chọn</span>
                                <span className={cx('value', 'seats')}>{bookingDetails.bookedSeats?.map(s => s.seatRow + s.seatCol).join(', ')}</span>
                            </div>
                        </div>

                        {/* --- TÓM TẮT GIÁ TIỀN --- */}
                        <div className={cx('pricing-summary')}>
                            {hasDiscount && (
                                <div className={cx('price-row')}>
                                    <span>Tạm tính</span>
                                    <span>{bookingDetails.originalAmount?.toLocaleString('vi-VN')}đ</span>
                                </div>
                            )}
                             {bookingDetails.discountApplied > 0 && (
                                <div className={cx('price-row', 'discount')}>
                                    <span>Khuyến mãi ({bookingDetails.promotionCodeApplied})</span>
                                    <span>- {bookingDetails.discountApplied?.toLocaleString('vi-VN')}đ</span>
                                </div>
                            )}
                            {/* --- HIỂN THỊ ĐIỂM SỬ DỤNG --- */}
                            {bookingDetails.pointsDiscount > 0 && (
                                <div className={cx('price-row', 'discount')}>
                                    <span>Giảm từ điểm ({bookingDetails.pointsUsed?.toLocaleString('vi-VN')} điểm)</span>
                                    <span>- {bookingDetails.pointsDiscount?.toLocaleString('vi-VN')}đ</span>
                                </div>
                            )}
                            <div className={cx('price-row', 'total')}>
                                <span>Tổng cộng</span>
                                <span>{bookingDetails.totalAmount?.toLocaleString('vi-VN')}đ</span>
                            </div>
                        </div>
                    </div>

                    {/* --- CỘT PHẢI - QR CODE & TRẠNG THÁI --- */}
                    <div className={cx('right-panel')}>
                        <div className={cx('qr-code-section')}>
                            <QRCodeSVG value={qrValue} size={160} level={"H"} includeMargin={true} bgColor="#ffffff" fgColor="#24283b"/>
                            <p>Dùng mã này để quét tại rạp</p>
                        </div>
                        <div className={cx('status-section')}>
                            <span className={cx('label')}>Trạng thái</span>
                            <span className={cx('value', statusInfo.className)}>{statusInfo.text}</span>
                        </div>
                         <div className={cx('payment-section')}>
                            <span className={cx('label')}>Thanh toán bằng</span>
                            <span className={cx('value', 'payment-method')}>{bookingDetails.paymentMethod?.methodName}</span>
                        </div>
                    </div>
                </div>
                
                {/* --- CÁC NÚT HÀNH ĐỘNG --- */}
                <div className={cx('ticket-actions')}>
                    {/* Thay thế CustomizeButton bằng button tiêu chuẩn */}
                    <button className={cx('btn', 'btn-secondary')} onClick={() => window.print()}>
                        <i className="fas fa-print"></i> In vé
                    </button>
                    <button className={cx('btn', 'btn-primary')} onClick={() => navigate('/booking/history')}>
                        <i className="fas fa-history"></i> Lịch sử đặt vé
                    </button>
                </div>
            </div>
        </div>
    );
};

export default BookingDetailPage;