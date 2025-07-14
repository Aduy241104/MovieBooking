import React, { useState, useEffect, useContext } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { getSeatStatus } from '../../service/ScreeningService';
import { getActivePaymentMethods } from '../../service/PaymentMethodService';
import { checkPromotion, createBooking, getUserPoints } from '../../service/BookingService'; // Thêm getUserPoints
import { AuthContext } from '../../context/AuthContext';
import SeatSelection from './SeatSelection/SeatSelection';
import OrderSummary from './OrderSummary/OrderSummary';
// import CustomizeButton from '../../components/CustomeButton/CustomizeButton'; // ĐÃ XÓA
import DefaultLayout from '../../layouts/DefaultLayout'; // GIẢ SỬ ĐƯỜNG DẪN ĐÚNG
import styles from './bookingPage.scss'; // ĐÃ ĐỔI TÊN FILE CSS CHO NHẤT QUÁN
import classNames from 'classnames/bind';
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';

const cx = classNames.bind(styles);

const BookingPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { user } = useContext(AuthContext);
    const { screeningInfo, movieInfo } = location.state || {};

    // States cho dữ liệu tải từ API
    const [seats, setSeats] = useState([]);
    const [paymentMethods, setPaymentMethods] = useState([]);
    const [userPoints, setUserPoints] = useState(0);

    // States cho lựa chọn của người dùng
    const [selectedSeats, setSelectedSeats] = useState([]);
    const [selectedPaymentMethodId, setSelectedPaymentMethodId] = useState('');
    const [promotionCode, setPromotionCode] = useState('');
    const [pointsToUse, setPointsToUse] = useState('');

    // States cho các giá trị tính toán
    const [totalPrice, setTotalPrice] = useState(0);
    const [discountAmount, setDiscountAmount] = useState(0);
    const [pointsDiscount, setPointsDiscount] = useState(0);
    const [finalPrice, setFinalPrice] = useState(0);

    // States cho trạng thái UI
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [checkingPromotion, setCheckingPromotion] = useState(false);
    const [promotionError, setPromotionError] = useState('');
    const [promotionSuccess, setPromotionSuccess] = useState('');
    const [pointsInputError, setPointsInputError] = useState('');
    const [appliedPromotion, setAppliedPromotion] = useState(null);

    // --- TOÀN BỘ LOGIC, USEEFFECT, HÀM HANDLE GIỮ NGUYÊN NHƯ CŨ ---
    // ... (logic của bạn được giữ nguyên ở đây) ...

    // Effect để tải tất cả dữ liệu cần thiết khi trang được mở
    useEffect(() => {
        if (!screeningInfo || !movieInfo || !user) {
            navigate(movieInfo?.id ? `/movie-detail/${movieInfo.id}` : '/');
            return;
        }

        const loadInitialData = async () => {
            setLoading(true);
            setError('');
            try {
                // Tải song song tất cả API calls
                const [seatRes, paymentRes, pointsRes] = await Promise.all([
                    getSeatStatus(screeningInfo.screeningId),
                    getActivePaymentMethods(),
                    getUserPoints()
                ]);

                setSeats(seatRes.data?.result || []);

                const activePayments = paymentRes.data?.result || [];
                setPaymentMethods(activePayments);
                if (activePayments.length > 0) {
                    setSelectedPaymentMethodId(activePayments[0].id.toString());
                }

                setUserPoints(pointsRes.data?.result || 0);

            } catch (err) {
                console.error("Error loading booking data:", err);
                setError(err.response?.data?.message || 'Không thể tải dữ liệu trang đặt vé.');
            } finally {
                setLoading(false);
            }
        };

        loadInitialData();
    }, [screeningInfo, movieInfo, user, navigate]);


    // Effect để tính toán lại giá tiền mỗi khi có sự thay đổi
    useEffect(() => {
        // 1. Tính tổng tiền gốc từ ghế đã chọn (originalTotalAmount)
        let currentTotal = 0;
        if (screeningInfo && screeningInfo.fareType) {
            const basePrice = parseFloat(screeningInfo.fareType.basePrice || 0);

            const movieFormat = screeningInfo.fareType.movieFormat || '';
            const timeSlotType = screeningInfo.fareType.timeSlotType || '';
            let formatSurcharge = 0;
            if (movieFormat.toUpperCase() === '3D') formatSurcharge = 20000;
            else if (movieFormat.toUpperCase() === 'IMAX') formatSurcharge = 35000;

            let timeSurcharge = 0;
            if (timeSlotType === 'Cuối Tuần') timeSurcharge = 10000;
            else if (timeSlotType === 'Ngày Lễ') timeSurcharge = 15000;

            selectedSeats.forEach(seat => {
                const seatTypePrice = parseFloat(seat.seatTypePrice || 0);
                currentTotal += basePrice + formatSurcharge + timeSurcharge + seatTypePrice;
            });
        }
        setTotalPrice(currentTotal);

        // 2. Tính giảm giá từ khuyến mãi
        let currentDiscount = 0;
        if (appliedPromotion && currentTotal > 0 && currentTotal >= parseFloat(appliedPromotion.minOrder)) {
            if (appliedPromotion.discountType === 'PERCENT') {
                currentDiscount = (currentTotal * parseFloat(appliedPromotion.discountLevel)) / 100;
                if (appliedPromotion.maxDiscount && currentDiscount > parseFloat(appliedPromotion.maxDiscount)) {
                    currentDiscount = parseFloat(appliedPromotion.maxDiscount);
                }
            } else if (appliedPromotion.discountType === 'AMOUNT') {
                currentDiscount = parseFloat(appliedPromotion.discountLevel);
            }
            if (currentDiscount > currentTotal) {
                currentDiscount = currentTotal;
            }
        }
        setDiscountAmount(currentDiscount);

        const finalTotalAfterPromotion = currentTotal - currentDiscount;

        // 3. Tính giảm giá từ điểm
        let currentPointsDiscount = 0;
        const pointsInputValue = parseInt(pointsToUse) || 0;
        if (pointsInputValue > 0) {
            const roundedPoints = Math.floor(pointsInputValue / 1000) * 1000;
            currentPointsDiscount = roundedPoints;
            if (currentPointsDiscount > finalTotalAfterPromotion) {
                currentPointsDiscount = Math.floor(finalTotalAfterPromotion / 1000) * 1000;
            }
        }
        setPointsDiscount(currentPointsDiscount);

        // 4. Tính giá cuối cùng
        setFinalPrice(Math.max(0, finalTotalAfterPromotion - currentPointsDiscount));

    }, [selectedSeats, appliedPromotion, pointsToUse, screeningInfo]);

    // Các hàm xử lý sự kiện (handle)
    const handleSeatSelect = (seatFromMap) => {
        setSelectedSeats(prevSeats => {
            const isSelected = prevSeats.find(s => s.seatId === seatFromMap.seatId);
            if (isSelected) {
                return prevSeats.filter(s => s.seatId !== seatFromMap.seatId);
            } else {
                return [...prevSeats, {
                    seatId: seatFromMap.seatId,
                    seatRow: seatFromMap.seatRow,
                    seatCol: seatFromMap.seatCol,
                    seatTypeName: seatFromMap.seatTypeName,
                    seatTypePrice: parseFloat(seatFromMap.seatTypePrice) || 0,
                }];
            }
        });
    };

     const handleApplyPromotion = async () => {
        if (!promotionCode.trim()) {
            setPromotionError('Vui lòng nhập mã khuyến mãi.');
            return;
        }
        setCheckingPromotion(true);
        setPromotionError('');
        setPromotionSuccess('');
        setAppliedPromotion(null);

        try {
            const response = await checkPromotion(promotionCode);

            // Kiểm tra xem API có trả về kết quả khuyến mãi không
            if (response.data && response.data.result) {
                const promoData = response.data.result;

                // Kiểm tra điều kiện tối thiểu ở frontend để đưa ra phản hồi ngay
                if (totalPrice < parseFloat(promoData.minOrder)) {
                    // Tạo thông báo lỗi trực tiếp
                    const minOrderFormatted = (Number(promoData.minOrder) || 0).toLocaleString('vi-VN');
                    setPromotionError(`Tổng tiền chưa đạt mức tối thiểu ${minOrderFormatted}đ của khuyến mãi.`);
                    return; // Dừng lại ở đây
                }

                // Nếu mọi thứ hợp lệ
                setAppliedPromotion(promoData);
                setPromotionSuccess('Áp dụng mã khuyến mãi thành công!');

            } else {
                // Nếu API trả về result là null hoặc không có, nghĩa là mã không hợp lệ
                setPromotionError('Mã khuyến mãi không hợp lệ hoặc đã hết hạn.');
            }
        } catch (err) {
            // Xử lý các lỗi kết nối hoặc lỗi server 500
            console.error("Error applying promotion:", err);
            setPromotionError('Không thể áp dụng mã khuyến mãi lúc này. Vui lòng thử lại.');
        } finally {
            setCheckingPromotion(false);
        }
    };

    const handlePointsInputChange = (e) => {
        const value = e.target.value;
        setPointsInputError('');
        if (value === '' || /^[0-9\b]+$/.test(value)) {
            const numValue = parseInt(value) || 0;
            if (numValue < 0) {
                setPointsInputError('Số điểm không được âm.');
                setPointsToUse('');
            } else if (numValue > userPoints) {
                setPointsInputError(`Bạn chỉ có ${userPoints.toLocaleString('vi-VN')} điểm.`);
                setPointsToUse(userPoints.toString());
            } else {
                setPointsToUse(value);
            }
        } else {
            setPointsInputError('Vui lòng chỉ nhập số.');
        }
    };

    

    const formatScreeningTime = (isoDateTimeString) => {
        if (!isoDateTimeString) return "N/A";
        try {
            return format(parseISO(isoDateTimeString), 'HH:mm - EEEE, dd/MM/yyyy', { locale: vi });
        } catch (error) {
            return "Thời gian không hợp lệ";
        }
    };

    // --- CÁC HÀM HELPER GIỮ NGUYÊN ---
    const getSubmitButtonText = () => {
        if (isSubmitting) return 'Đang xử lý...';
        if (selectedSeats.length === 0) return 'Thanh Toán';
        if (finalPrice > 0) return `Thanh Toán ${finalPrice.toLocaleString('vi-VN')}đ`;
        return 'Xác nhận đặt vé (Miễn phí)';
    };

    const handleCancelPromotion = () => {
        setAppliedPromotion(null);
        setPromotionCode('');
        setPromotionError('');
        setPromotionSuccess('');
    };

    const handleCancelPoints = () => {
        setPointsToUse('');
        setPointsInputError('');
    };

    // Hàm này giúp xóa lỗi khi người dùng bắt đầu nhập lại
    const handlePromotionCodeChange = (e) => {
        setPromotionCode(e.target.value);
        if (promotionError) setPromotionError('');
        if (promotionSuccess) setPromotionSuccess('');
    };

// <<< TẠO HÀM XỬ LÝ LỖI TẬP TRUNG >>>
    const handleBookingError = (rawMessage) => {
        const defaultMessage = 'Có lỗi xảy ra, vui lòng thử lại.';
        if (!rawMessage) {
            alert(defaultMessage);
            return;
        }

        const parts = rawMessage.split(':');
        const errorCode = parts[0];
        const errorValue = parts[1];

        switch (errorCode) {
            case 'INVALID_SEAT_SELECTION_SINGLE_GAP':
                alert('Lựa chọn ghế không hợp lệ. Vui lòng không để lại một ghế trống ở giữa.');
                break;
            case 'SEAT_ALREADY_BOOKED':
                alert(`Rất tiếc, ghế ${errorValue} đã có người khác đặt. Trang sẽ tự động tải lại sơ đồ ghế.`);
                // Tùy chọn: Tải lại sơ đồ ghế để cập nhật
                // loadSeats(); 
                break;
            case 'PROMOTION_MIN_ORDER_NOT_MET':
                const minOrder = Number(errorValue) || 0;
                alert(`Tổng tiền chưa đạt mức tối thiểu ${minOrder.toLocaleString('vi-VN')}đ của khuyến mãi.`);
                break;
            case 'PROMOTION_INVALID_OR_EXPIRED':
                alert('Mã khuyến mãi không hợp lệ hoặc đã hết hạn.');
                break;

            case 'POINTS_EXCEEDED':
                alert('Số điểm sử dụng vượt quá số điểm hiện có của bạn.');
                break;
            default:
                // Hiển thị các lỗi khác mà không có mã cụ thể
                alert(rawMessage);
                break;
        }
    };

    const handleSubmitBooking = async () => {
    // 1. Kiểm tra đầu vào cơ bản ở frontend
    if (selectedSeats.length === 0) {
        alert('Vui lòng chọn ít nhất một ghế.');
        return;
    }
    if (!selectedPaymentMethodId) {
        alert('Vui lòng chọn phương thức thanh toán.');
        return;
    }

    setIsSubmitting(true);

    // 2. Chuẩn bị payload để gửi lên backend
    const bookingPayload = {
        screeningId: screeningInfo.screeningId,
        seatIds: selectedSeats.map(s => s.seatId),
        promotionCode: appliedPromotion ? appliedPromotion.code : null,
        paymentMethodId: parseInt(selectedPaymentMethodId),
        pointsToUse: parseInt(pointsToUse) || 0,
    };

    try {
        // 3. Gọi API tạo booking
        const response = await createBooking(bookingPayload);
        const bookingResult = response.data.result;

        // 4. Xử lý kết quả thành công
        if (response.data.status === 201 && bookingResult) {
            if (bookingResult.paymentUrl) {
                // Nếu có URL thanh toán, lưu thông tin cần thiết và chuyển hướng
                localStorage.setItem('lastMovieInfoForBooking', JSON.stringify(movieInfo));
                // Lưu bookingId để có thể hủy nếu người dùng nhấn back
                localStorage.setItem('pendingBookingId', bookingResult.bookingId.toString());
                
                window.location.href = bookingResult.paymentUrl;
            } else {
                // Trường hợp thanh toán 100% bằng điểm, không có URL
                navigate('/booking/success', { state: { bookingId: bookingResult.bookingId } });
            }
        } else {
            // Xử lý các lỗi logic không mong muốn khác từ backend (ít khi xảy ra nếu status là 201)
            alert(response.data.message || 'Có lỗi xảy ra, vui lòng thử lại.');
        }

    } catch (err) {
        // 5. Bắt và xử lý các lỗi Exception từ backend (quan trọng nhất)
        const rawMessage = err.response?.data?.message || 'UNKNOWN_ERROR';
        
        // --- LOGIC XỬ LÝ LỖI ĐẶT TRÙNG ---
        if (rawMessage.startsWith('PENDING_BOOKING_EXISTS')) {
            // Hỏi người dùng có muốn đến trang lịch sử để thanh toán không
            if (window.confirm('Bạn đã có một đặt vé cho suất chiếu này đang chờ thanh toán. Bạn có muốn đi đến trang Lịch sử đặt vé để hoàn tất không?')) {
                navigate('/booking/history');
            }
        } 
        // --- XỬ LÝ CÁC LỖI KHÁC ---
        else if (rawMessage === 'INVALID_SEAT_SELECTION_SINGLE_GAP') {
            alert('Lựa chọn ghế không hợp lệ. Vui lòng không để lại một ghế trống ở giữa.');
        } else if (rawMessage.startsWith('SEAT_ALREADY_BOOKED')) {
            const seatName = rawMessage.split(':')[1];
            alert(`Rất tiếc, ghế ${seatName} đã có người khác đặt. Vui lòng chọn lại.`);
            // Có thể thêm logic tải lại sơ đồ ghế ở đây
        } else {
            // Các lỗi chung khác
            alert('Đã có lỗi xảy ra trong quá trình đặt vé. Vui lòng thử lại.');
            console.error("Booking failed with message:", rawMessage);
        }

    } finally {
        setIsSubmitting(false);
    }
};

    const canApplyDiscount = selectedSeats.length > 0;
    // --- HIỂN THỊ TRẠNG THÁI LOADING/ERROR TRONG LAYOUT ---
    if (loading || error) {
        return (
            <DefaultLayout>
                <div className={cx('page-status-container')}>
                    {loading && <p>Đang tải dữ liệu trang đặt vé...</p>}
                    {error && <p className={cx('error-message')}>{error}</p>}
                </div>
            </DefaultLayout>
        );
    }

    return (
        <DefaultLayout>
            <div className={cx('page-background')}>
                <div className={cx('booking-container')}>
                    {/* --- CỘT TRÁI - CHỌN GHẾ --- */}
                    <div className={cx('main-content')}>
                        <div className={cx('section', 'movie-info-section')}>
                            <div className={cx('movie-poster')}>
                                <img src={movieInfo?.smallImage || 'https://via.placeholder.com/150x220'} alt={movieInfo?.nameVN} />
                            </div>
                            <div className={cx('movie-details')}>
                                <h4>{movieInfo?.nameVN || 'Tên phim'}</h4>
                                <p><strong>Suất chiếu:</strong> {formatScreeningTime(screeningInfo?.showDateTime)}</p>
                                <p><strong>Phòng chiếu:</strong> {screeningInfo?.cinemaRoom?.cinemaRoomName || 'N/A'}</p>
                                <p><strong>Định dạng:</strong> {screeningInfo?.fareType?.movieFormat || 'N/A'}</p>
                            </div>
                        </div>
                        <div className={cx('section', 'seat-selection-section')}>
                            <div className={cx('timeline')}>
                                <div className={cx('timeline-item', 'active')}>Chọn ghế</div>
                                <div className={cx('timeline-item')}>Thanh toán</div>
                                <div className={cx('timeline-item')}>Hoàn tất</div>
                            </div>
                            <SeatSelection
                                seatsData={seats}
                                selectedSeats={selectedSeats}
                                onSeatSelect={handleSeatSelect}
                                screeningInfo={screeningInfo}
                            />
                        </div>
                    </div>

                    {/* --- CỘT PHẢI - THANH TOÁN --- */}
                    <div className={cx('sidebar')}>
                        <div className={cx('sidebar-content')}>
                            <OrderSummary
                                movieInfo={movieInfo}
                                screeningInfo={screeningInfo}
                                selectedSeats={selectedSeats}
                                totalPrice={totalPrice}
                                discountAmount={discountAmount}
                                pointsDiscount={pointsDiscount}
                                finalPrice={finalPrice}
                            />

                            {/* --- KHU VỰC GIẢM GIÁ --- */}
                            <div className={cx('discount-area', { 'disabled': !canApplyDiscount })}>
                                {/* --- ĐIỂM THƯỞNG --- */}
                                <div className={cx('discount-section')}>
                                    <div className={cx('section-header')}>
                                        <h5><i className="fas fa-star"></i> Sử dụng điểm</h5>
                                        {pointsToUse && <button onClick={handleCancelPoints} className={cx('btn-cancel')}>Hủy</button>}
                                    </div>
                                    <p className={cx('points-available')}>Điểm khả dụng: <strong>{userPoints.toLocaleString('vi-VN')}</strong></p>
                                    <div className={cx('input-group')}>
                                        <input type="text" className={cx('form-control', { 'is-invalid': pointsInputError })} placeholder="Nhập số điểm" value={pointsToUse} onChange={handlePointsInputChange} disabled={!canApplyDiscount} />
                                    </div>
                                    {pointsInputError && <div className={cx('invalid-feedback')}>{pointsInputError}</div>}
                                </div>

                                {/* --- MÃ KHUYẾN MÃI --- */}
                                <div className={cx('discount-section')}>
                                    <div className={cx('section-header')}>
                                        <h5><i className="fas fa-tags"></i> Mã khuyến mãi</h5>
                                        {appliedPromotion && <button onClick={handleCancelPromotion} className={cx('btn-cancel')}>Hủy mã</button>}
                                    </div>
                                    {!appliedPromotion ? (
                                        <div className={cx('input-group')}>
                                            <input
                                                type="text"
                                                className={cx('form-control')}
                                                placeholder="Nhập mã"
                                                value={promotionCode}
                                                onChange={handlePromotionCodeChange} // Sử dụng hàm mới
                                                disabled={checkingPromotion || !canApplyDiscount}
                                            />
                                            <button
                                                className={cx('btn', 'btn-apply')}
                                                onClick={handleApplyPromotion}
                                                disabled={checkingPromotion || !promotionCode.trim() || !canApplyDiscount}>
                                                Áp dụng
                                            </button>
                                        </div>
                                    ) : (
                                        <div className={cx('applied-promo-info')}>
                                            <span>Đã áp dụng mã: <strong>{appliedPromotion.code}</strong></span>
                                        </div>
                                    )}
                                    {promotionError && <p className={cx('error-feedback')}>{promotionError}</p>}
                                    {promotionSuccess && <p className={cx('success-feedback')}>{promotionSuccess}</p>}
                                </div>
                            </div>

                            {/* --- THANH TOÁN --- */}
                            <div className={cx('payment-section')}>
                                <h5><i className="fas fa-credit-card"></i> Phương thức thanh toán</h5>
                                {paymentMethods.map(method => (
                                    <div className={cx('form-check')} key={method.id}>
                                        <input className={cx('form-check-input')} type="radio" name="paymentMethod" id={`paymentMethod-${method.id}`} value={method.id.toString()} checked={selectedPaymentMethodId === method.id.toString()} onChange={(e) => setSelectedPaymentMethodId(e.target.value)} />
                                        <label className={cx('form-check-label')} htmlFor={`paymentMethod-${method.id}`}>{method.name}</label>
                                    </div>
                                ))}
                            </div>

                            {/* NÚT SUBMIT ĐÃ ĐƯỢC THAY THẾ */}
                            <button
                                onClick={handleSubmitBooking}
                                disabled={isSubmitting || selectedSeats.length === 0 || !selectedPaymentMethodId}
                                className={cx('btn', 'btn-submit-booking')}
                            >
                                {getSubmitButtonText()}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </DefaultLayout>
    );
};

export default BookingPage;