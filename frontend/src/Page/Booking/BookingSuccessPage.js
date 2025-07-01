// src/pages/BookingSuccessPage/BookingSuccessPage.js
import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { getBookingDetails } from '../../service/BookingService'; // KIỂM TRA ĐƯỜNG DẪN
import CustomizeButton from '../../components/CustomeButton/CustomizeButton'; // KIỂM TRA ĐƯỜNG DẪN
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';

const BookingSuccessPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const queryParams = new URLSearchParams(location.search); // Lấy query params từ URL

    // Ưu tiên lấy bookingId từ query params (khi redirect từ backend sau VNPAY)
    // Sau đó mới thử lấy từ location.state (khi navigate nội bộ từ BookingPage cho thanh toán tại quầy)
    const bookingIdFromQuery = queryParams.get('bookingId');
    const bookingIdFromState = location.state?.bookingId; // state?.bookingId để tránh lỗi nếu state là null
    const bookingId = bookingIdFromQuery || bookingIdFromState;

    console.log('BookingSuccessPage - location.search:', location.search);
    console.log('BookingSuccessPage - bookingIdFromQuery:', bookingIdFromQuery);
    console.log('BookingSuccessPage - bookingIdFromState:', bookingIdFromState);
    console.log('BookingSuccessPage - Final bookingId to use:', bookingId);

    const [bookingDetails, setBookingDetails] = useState(null);
    const [loading, setLoading] = useState(true); // Bắt đầu với loading=true
    const [error, setError] = useState(''); // Thêm state cho lỗi

    useEffect(() => {
        if (!bookingId) {
            console.warn('BookingSuccessPage: No bookingId found from query params or state. Navigating to home.');
            navigate('/'); // Không có bookingId, về trang chủ
            return;
        }

        setLoading(true); // Set loading khi bắt đầu fetch
        setError(''); // Reset lỗi cũ
        const fetchDetails = async () => {
            try {
                const response = await getBookingDetails(Number(bookingId)); // Chuyển sang Number nếu API cần
                if (response.data && response.data.result) {
                    console.log('BookingSuccessPage - Fetched booking details:', response.data.result);
                    setBookingDetails(response.data.result);
                } else {
                    console.warn("BookingSuccessPage - Could not fetch booking details for ID:", bookingId, "Response data:", response.data);
                    setError("Không thể tải chi tiết đặt vé (dữ liệu không hợp lệ).");
                }
            } catch (err) {
                console.error("BookingSuccessPage - Error fetching booking details:", err);
                setError(err.response?.data?.message || "Lỗi khi tải chi tiết đặt vé.");
            } finally {
                setLoading(false);
            }
        };

        fetchDetails();
    }, [bookingId, navigate]); // Phụ thuộc vào bookingId để fetch lại nếu nó thay đổi

    const formatScreeningDateTime = (dateTime) => {
        if (!dateTime) return 'N/A';
        try {
            return format(parseISO(dateTime), 'HH:mm - EEEE, dd/MM/yyyy', { locale: vi });
        } catch (e) {
            console.error("Error formatting screening date time:", e, dateTime);
            return 'N/A';
        }
    };

    if (loading) {
        return <div className="container mt-5 text-center"><p>Đang tải thông tin đặt vé...</p></div>;
    }

    if (error) { // Hiển thị lỗi nếu có
        return (
            <div className="container mt-5 text-center">
                <i className="fas fa-exclamation-triangle fa-3x text-warning mb-3"></i>
                <h2>Đã xảy ra lỗi</h2>
                <p>{error}</p>
                <Link to="/">
                    <CustomizeButton primary large className="mt-3">
                        Về Trang Chủ
                    </CustomizeButton>
                </Link>
            </div>
        );
    }

    if (!bookingDetails) {
        // Trường hợp này xảy ra nếu không có lỗi nhưng bookingDetails vẫn null (ít khi)
        // Hoặc nếu !bookingId ở useEffect đã navigate đi rồi.
        // Nếu muốn hiển thị thông báo khác biệt:
        return (
            <div className="container mt-5 text-center">
                <h2>Thông tin không khả dụng</h2>
                <p>Không tìm thấy chi tiết cho đơn đặt vé #{bookingId}. Vui lòng kiểm tra lại hoặc liên hệ hỗ trợ.</p>
                <Link to="/">
                    <CustomizeButton primary large className="mt-3">
                        Về Trang Chủ
                    </CustomizeButton>
                </Link>
            </div>
        );
    }

    return (
        <div className="container mt-5 text-center">
            <i className="fas fa-check-circle fa-5x text-success mb-3"></i>
            <h2>Đặt vé thành công!</h2>
            <p>Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi.</p>
            <p>Mã đặt vé của bạn là: <strong>#{bookingDetails.bookingId}</strong></p>

            <div className="card mt-4 mx-auto" style={{ maxWidth: '600px' }}>
                <div className="card-header fw-bold"> {/* Thêm fw-bold cho tiêu đề card */}
                    Chi tiết vé
                </div>
                <div className="card-body text-start"> {/* text-start để chữ căn trái */}
                    <p><strong>Phim:</strong> {bookingDetails.screening?.movieNameVn || 'N/A'}</p>
                    <p><strong>Suất chiếu:</strong> {formatScreeningDateTime(bookingDetails.screening?.showDateTime)}</p>
                    <p><strong>Phòng chiếu:</strong> {bookingDetails.screening?.cinemaRoomName || 'N/A'}</p>
                    <p><strong>Ghế:</strong> {bookingDetails.bookedSeats?.map(s => `${s.seatRow}${s.seatCol}`).join(', ') || 'N/A'}</p>
                    <p><strong>Tổng tiền:</strong> {(bookingDetails.totalAmount || 0).toLocaleString('vi-VN')}đ</p>
                    <p><strong>Trạng thái:</strong> <span className={`badge bg-${bookingDetails.bookingStatus === 'PAID' ? 'success' : 'warning'}`}>{bookingDetails.bookingStatus || 'N/A'}</span></p>
                    {bookingDetails.paymentMethod?.methodName && <p><strong>Thanh toán:</strong> {bookingDetails.paymentMethod.methodName}</p>}
                </div>
            </div>

            <div className="mt-4 d-flex justify-content-center gap-3"> {/* d-flex và gap cho nút */}
                <Link to="/booking/history">
                    <CustomizeButton secondary large > {/* Bỏ me-3 nếu dùng gap */}
                        Xem Lịch Sử Đặt Vé
                    </CustomizeButton>
                </Link>
                <Link to="/">
                    <CustomizeButton primary large>
                        Về Trang Chủ
                    </CustomizeButton>
                </Link>
            </div>
        </div>
    );
};

export default BookingSuccessPage;