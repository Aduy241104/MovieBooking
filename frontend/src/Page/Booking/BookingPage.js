import React, { useState, useEffect, useContext } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { getSeatStatus } from '../../service/ScreeningService';
import { getActivePaymentMethods } from '../../service/PaymentMethodService';
import { checkPromotion, createBooking, getUserPoints } from '../../service/BookingService'; // Thêm getUserPoints
import { AuthContext } from '../../context/AuthContext';
import SeatSelection from './SeatSelection/SeatSelection';
import OrderSummary from './OrderSummary/OrderSummary';
import CustomizeButton from '../../components/CustomeButton/CustomizeButton';
import styles from './bookingPage.scss'; // Đảm bảo tên file CSS/SCSS khớp
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
            if (numValue > userPoints) {
                setPointsInputError(`Bạn chỉ có ${userPoints.toLocaleString('vi-VN')} điểm.`);
                setPointsToUse(userPoints.toString());
            } else {
                setPointsToUse(value);
            }
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

    if (loading) {
        return <div className="container mt-5 text-center"><p>Đang tải dữ liệu trang đặt vé...</p></div>;
    }
    if (error) {
        return <div className="container mt-5 text-center"><p className="text-danger">{error}</p></div>;
    }

    return (
        <div className={cx('booking-container', 'container', 'mt-4', 'mb-5')}>
            <h2 className={cx('page-title')}>Đặt Vé Xem Phim</h2>
            <div className="row">
                <div className="col-lg-8">
                    <div className={cx('section', 'movie-info-section')}>
                        <h4>{movieInfo?.nameVN || 'Tên phim'}</h4>
                        <p><i className="fas fa-calendar-alt"></i> Suất chiếu: {formatScreeningTime(screeningInfo?.showDateTime)}</p>
                        <p><i className="fas fa-video"></i> Phòng chiếu: {screeningInfo?.cinemaRoom?.cinemaRoomName || 'N/A'}</p>
                        <p><i className="fas fa-film"></i> Định dạng: {screeningInfo?.fareType?.movieFormat || 'N/A'}</p>
                    </div>
                    <div className={cx('section', 'seat-selection-section')}>
                        <h4>Chọn ghế</h4>
                        <SeatSelection
                            seatsData={seats}
                            selectedSeats={selectedSeats}
                            onSeatSelect={handleSeatSelect}
                            screeningInfo={screeningInfo} // Truyền screeningInfo vào SeatSelection
                        />
                    </div>
                </div>
                <div className="col-lg-4">
                    <OrderSummary
                        movieInfo={movieInfo}
                        screeningInfo={screeningInfo}
                        selectedSeats={selectedSeats}
                        totalPrice={totalPrice}
                        discountAmount={discountAmount}
                        pointsDiscount={pointsDiscount}
                        finalPrice={finalPrice}
                    />
                    <div className={cx('section', 'points-section', 'mt-3')}>
                        <h4>Sử dụng điểm thưởng</h4>
                        <p className="text-muted small">Điểm khả dụng: <strong>{userPoints.toLocaleString('vi-VN')}</strong> (1 điểm = 1đ)</p>
                        <div className="input-group">
                            <input
                                type="text"
                                className={`form-control ${pointsInputError ? 'is-invalid' : ''}`}
                                placeholder="Nhập số điểm"
                                value={pointsToUse}
                                onChange={handlePointsInputChange}
                            />
                        </div>
                        {pointsInputError && <div className="invalid-feedback d-block">{pointsInputError}</div>}
                        {pointsDiscount > 0 && <p className="text-success small mt-2">Áp dụng giảm {pointsDiscount.toLocaleString('vi-VN')}đ.</p>}
                    </div>
                    <div className={cx('section', 'promotion-section', 'mt-3')}>
                        <h4>Mã khuyến mãi</h4>
                        <div className="input-group mb-2">
                            <input type="text" className="form-control" placeholder="Nhập mã" value={promotionCode} onChange={(e) => setPromotionCode(e.target.value)} disabled={checkingPromotion || totalPrice === 0}/>
                            <CustomizeButton primary onClick={handleApplyPromotion} disabled={checkingPromotion || !promotionCode.trim() || totalPrice === 0}>
                                {checkingPromotion ? '...' : 'Áp dụng'}
                            </CustomizeButton>
                        </div>
                        {promotionError && <p className="text-danger small mt-1">{promotionError}</p>}
                        {promotionSuccess && <p className="text-success small mt-1">{promotionSuccess}</p>}
                    </div>
                    <div className={cx('section', 'payment-section', 'mt-3')}>
                        <h4>Phương thức thanh toán</h4>
                        {paymentMethods.map(method => (
                            <div className="form-check" key={method.id}>
                                <input className="form-check-input" type="radio" name="paymentMethod" id={`paymentMethod-${method.id}`} value={method.id.toString()} checked={selectedPaymentMethodId === method.id.toString()} onChange={(e) => setSelectedPaymentMethodId(e.target.value)} />
                                <label className="form-check-label" htmlFor={`paymentMethod-${method.id}`}>{method.name}</label>
                            </div>
                        ))}
                    </div>
                    <CustomizeButton gold large block onClick={handleSubmitBooking} disabled={isSubmitting || selectedSeats.length === 0 || !selectedPaymentMethodId}>
                        {isSubmitting ? 'Đang xử lý...' : `Thanh Toán ${finalPrice > 0 ? finalPrice.toLocaleString('vi-VN') + 'đ' : 'Miễn phí'}`}
                    </CustomizeButton>
                </div>
            </div>
        </div>
    );
};

export default BookingPage;