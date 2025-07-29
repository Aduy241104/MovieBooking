import React, { useState, useEffect, useContext } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { getSeatStatus } from '../../service/ScreeningService';
import { getActivePaymentMethods } from '../../service/PaymentMethodService';
import { checkPromotion, createBooking, getUserPoints } from '../../service/BookingService'; 
import { AuthContext } from '../../context/AuthContext';
import SeatSelection from './SeatSelection/SeatSelection';
import OrderSummary from './OrderSummary/OrderSummary';
import DefaultLayout from '../../layouts/DefaultLayout'; 
import styles from './bookingPage.scss'; 
import classNames from 'classnames/bind';
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';
import { message, Modal } from 'antd';
const cx = classNames.bind(styles);

const BookingPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { user } = useContext(AuthContext);
    const { screeningInfo, movieInfo } = location.state || {};

   // States for data loaded from API
    const [seats, setSeats] = useState([]);
    const [paymentMethods, setPaymentMethods] = useState([]);
    const [userPoints, setUserPoints] = useState(0);

    // States for user selection
    const [selectedSeats, setSelectedSeats] = useState([]);
    const [selectedPaymentMethodId, setSelectedPaymentMethodId] = useState('');
    const [promotionCode, setPromotionCode] = useState('');
    const [pointsToUse, setPointsToUse] = useState('');

    // States for computed values
    const [totalPrice, setTotalPrice] = useState(0);
    const [discountAmount, setDiscountAmount] = useState(0);
    const [pointsDiscount, setPointsDiscount] = useState(0);
    const [finalPrice, setFinalPrice] = useState(0);

    // States for UI state
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [checkingPromotion, setCheckingPromotion] = useState(false);
    const [promotionError, setPromotionError] = useState('');
    const [promotionSuccess, setPromotionSuccess] = useState('');
    const [pointsInputError, setPointsInputError] = useState('');
    const [appliedPromotion, setAppliedPromotion] = useState(null);

    useEffect(() => {
        if (!screeningInfo || !movieInfo || !user) {
            navigate(movieInfo?.id ? `/movie-detail/${movieInfo.id}` : '/');
            return;
        }

        const loadInitialData = async () => {
            setLoading(true);
            setError('');
            try {
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


    // Effect to recalculate price every time there is a change
    useEffect(() => {
        // 1. Calculate the original total amount from the selected seat (originalTotalAmount)
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

        // 2. Calculate discount from promotion
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

       // 3. Calculate discount from point 
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

       // 4. Calculate final price
        setFinalPrice(Math.max(0, finalTotalAfterPromotion - currentPointsDiscount));

    }, [selectedSeats, appliedPromotion, pointsToUse, screeningInfo]);

  // Event handler functions
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
   
    // Update UI status to start the process
    setCheckingPromotion(true);
    setPromotionError('');
    setPromotionSuccess('');
    setAppliedPromotion(null);

    try {
        // Call API - This is the only interaction with backend
        const response = await checkPromotion(promotionCode);

        // Handling when API returns successfully (http 200)
        const promoData = response.data.result;

        // Logic check depends on the current state of the order (Totalprice)
        if (totalPrice < parseFloat(promoData.minOrder)) {
            const minOrderFormatted = (Number(promoData.minOrder) || 0).toLocaleString('vi-VN');
            const errorMessage = `Tổng tiền chưa đạt mức tối thiểu ${minOrderFormatted}đ của khuyến mãi.`;
            
            setPromotionError(errorMessage); 
            message.error(errorMessage);     
            return; 
        }

        // If all conditions are satisfied
        setAppliedPromotion(promoData);
        setPromotionSuccess('Áp dụng mã khuyến mãi thành công!');
        message.success('Áp dụng mã khuyến mãi thành công!');

    } catch (err) {
       // 5. Handling when API returns error (http 4xx, 5xx) 
// All professional errors from the backend (wrong code, expired)
        console.error("Error applying promotion:", err);
        
       // Take the exact error message from Apiresponse that GlobalexceptiHandler created
        const errorMessage = err.response?.data?.message || 'Không thể kết nối hoặc có lỗi xảy ra.';
        setPromotionError(errorMessage); 
        message.error(errorMessage);    
        
    } finally {
        // 6. Update the UI state after the ending process
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

  // Erase the error when the user starts to enter again
    const handlePromotionCodeChange = (e) => {
        setPromotionCode(e.target.value);
        if (promotionError) setPromotionError('');
        if (promotionSuccess) setPromotionSuccess('');
    };

    const handleSubmitBooking = async () => {
        if (selectedSeats.length === 0) {
            message.warning('Vui lòng chọn ít nhất một ghế.');
            return;
        }
        if (!selectedPaymentMethodId) {
            message.warning('Vui lòng chọn phương thức thanh toán.');
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
                    localStorage.setItem('lastMovieInfoForBooking', JSON.stringify(movieInfo));
                    window.location.href = bookingResult.paymentUrl;
                } else {
                    navigate('/booking/success', { state: { bookingId: bookingResult.bookingId } });
                }
            } else {
                message.error(response.data.message || 'Có lỗi xảy ra, vui lòng thử lại.');
            }

        } catch (err) {
            const errorResponse = err.response?.data;
            const rawMessage = errorResponse?.message || 'UNKNOWN_ERROR';
            if (rawMessage.startsWith('PENDING_BOOKING_EXISTS')) {
                Modal.confirm({
                    title: 'Đặt vé đang chờ xử lý',
                    content: 'Bạn đã có một đặt vé cho suất chiếu này đang chờ thanh toán. Bạn có muốn đến trang Lịch sử đặt vé để hoàn tất không?',
                    okText: 'Đến trang lịch sử',
                    cancelText: 'Ở lại',
                    onOk() {
                        navigate('/profile/booking-history');
                    },
                });
            } else {
                message.error(rawMessage);
                if (rawMessage.toLowerCase().includes('ghế') && rawMessage.toLowerCase().includes('đã có người khác chọn')) {
                    handleSeatExpire(); // Refetch
                }
            }
        } finally {
            setIsSubmitting(false);
        }
    };

   const handleSeatExpire = () => {
        getSeatStatus(screeningInfo.screeningId)
            .then(response => {
                if (response.data && response.data.result) {
                    setSeats(response.data.result);
                }
            })
            .catch(err => console.error("Error refetching seats after expiration:", err));
    };

    const canApplyDiscount = selectedSeats.length > 0;
    // Display Loading/Error status in layout 
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
                    {/* left column-Choose */}
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
                                 onSeatExpire={handleSeatExpire} 
                            />
                        </div>
                    </div>

                    {/* Right column-Payment */}
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

                           {/* Discount area */}
                            <div className={cx('discount-area', { 'disabled': !canApplyDiscount })}>
                               {/* Bonus point */}
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

                               {/* Promotion code */}
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
                                                onChange={handlePromotionCodeChange} 
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

                            {/* PAY */}
                            <div className={cx('payment-section')}>
                                <h5><i className="fas fa-credit-card"></i> Phương thức thanh toán</h5>
                                {paymentMethods.map(method => (
                                    <div className={cx('form-check')} key={method.id}>
                                        <input className={cx('form-check-input')} type="radio" name="paymentMethod" id={`paymentMethod-${method.id}`} value={method.id.toString()} checked={selectedPaymentMethodId === method.id.toString()} onChange={(e) => setSelectedPaymentMethodId(e.target.value)} />
                                        <label className={cx('form-check-label')} htmlFor={`paymentMethod-${method.id}`}>{method.name}</label>
                                    </div>
                                ))}
                            </div>

                            {/* SUBMIT BUTTON */}
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