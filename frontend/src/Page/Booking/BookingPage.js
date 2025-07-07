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
            if (response.data && response.data.status === 200 && response.data.result) {
                const promoData = response.data.result;
                if (totalPrice < parseFloat(promoData.minOrder)) {
                    setPromotionError(`Tổng tiền ${totalPrice.toLocaleString('vi-VN')}đ không đủ điều kiện tối thiểu ${parseFloat(promoData.minOrder).toLocaleString('vi-VN')}đ.`);
                    return;
                }
                setAppliedPromotion(promoData);
                setPromotionSuccess('Áp dụng thành công!');
            } else {
                setPromotionError(response.data?.message || 'Mã không hợp lệ.');
            }
        } catch (err) {
            setPromotionError(err.response?.data?.message || 'Lỗi khi áp dụng mã.');
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
    const handleSubmitBooking = async () => {
        if (selectedSeats.length === 0 || !selectedPaymentMethodId) {
            alert('Vui lòng chọn ghế và phương thức thanh toán.');
            return;
        }

        setIsSubmitting(true);
        const bookingPayload = {
            screeningId: screeningInfo.screeningId,
            seatIds: selectedSeats.map(s => s.seatId),
            promotionCode: appliedPromotion ? appliedPromotion.code : null,
            paymentMethodId: parseInt(selectedPaymentMethodId),
            pointsToUse: parseInt(pointsToUse) || 0,
        };

        try {
            const response = await createBooking(bookingPayload);
            const bookingResult = response.data.result;
            if (response.data.status === 201 && bookingResult) {
                if (bookingResult.paymentUrl) {
                    // Lưu movieInfo để dùng ở trang Failure nếu cần
                    localStorage.setItem('lastMovieInfoForBooking', JSON.stringify(movieInfo));
                    window.location.href = bookingResult.paymentUrl;
                } else {
                    navigate('/booking/success', { state: { bookingId: bookingResult.bookingId } });
                }
            } else {
                alert(response.data.message || 'Đặt vé không thành công.');
            }
        } catch (err) {
            alert(err.response?.data?.message || 'Có lỗi xảy ra khi đặt vé.');
        } finally {
            setIsSubmitting(false);
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

    const handlePromotionCodeChange = (e) => {
        // Cập nhật giá trị của mã khuyến mãi vào state
        setPromotionCode(e.target.value);

        // CHÌA KHÓA: Nếu có lỗi đang hiển thị, hãy xóa nó đi
        if (promotionError) {
            setPromotionError('');
        }
        // Nếu có thông báo thành công đang hiển thị, cũng xóa luôn
        if (promotionSuccess) {
            setPromotionSuccess('');
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