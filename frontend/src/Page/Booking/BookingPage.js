import React, { useState, useEffect, useContext } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { getSeatStatus } from '../../service/ScreeningService'; // TODO: KIỂM TRA ĐƯỜNG DẪN
import { getActivePaymentMethods } from '../../service/PaymentMethodService'; // TODO: KIỂM TRA ĐƯỜNG DẪN
import { checkPromotion, createBooking } from '../../service/BookingService'; // TODO: KIỂM TRA ĐƯỜNG DẪN
import { AuthContext } from '../../context/AuthContext'; // TODO: KIỂM TRA ĐƯỜNG DẪN
import SeatSelection from './SeatSelection/SeatSelection';
import OrderSummary from './OrderSummary/OrderSummary';
import CustomizeButton from '../../components/CustomeButton/CustomizeButton'; // TODO: KIỂM TRA ĐƯỜNG DẪN
import styles from './bookingPage.scss'; // Sử dụng .module.scss nếu bạn đặt tên file như vậy
import classNames from 'classnames/bind';
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';

const cx = classNames.bind(styles);

const BookingPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { user } = useContext(AuthContext);

    // Lấy screeningInfo và movieInfo từ state của navigation
    // screeningInfo nên chứa: { screeningId, time (HH:mm:ss), cinemaRoom, fareType, movieFormat, showDateTime (ISO string đầy đủ) }

    const [seats, setSeats] = useState([]);
    const [selectedSeats, setSelectedSeats] = useState([]); // [{ seatId, seatRow, seatCol, seatTypeName, seatTypePrice }, ...]
    const [loadingSeats, setLoadingSeats] = useState(false);
    const [errorSeats, setErrorSeats] = useState('');

    const [paymentMethods, setPaymentMethods] = useState([]);
    const [selectedPaymentMethodId, setSelectedPaymentMethodId] = useState('');
    const [loadingPaymentMethods, setLoadingPaymentMethods] = useState(false);

    const [promotionCode, setPromotionCode] = useState('');
    const [appliedPromotion, setAppliedPromotion] = useState(null);
    const [checkingPromotion, setCheckingPromotion] = useState(false);
    const [promotionError, setPromotionError] = useState('');
    const [promotionSuccess, setPromotionSuccess] = useState('');

    const [totalPrice, setTotalPrice] = useState(0); // Tổng tiền gốc của các ghế đã chọn
    const [discountAmount, setDiscountAmount] = useState(0); // Số tiền được giảm
    const [finalPrice, setFinalPrice] = useState(0); // Tổng tiền sau khi giảm giá

    const [isSubmitting, setIsSubmitting] = useState(false);
const { screeningInfo, movieInfo } = location.state || {}; // movieInfo ở đây
    useEffect(() => {
        if (!screeningInfo || !movieInfo || !user || !screeningInfo.screeningId || !movieInfo.id) {
            console.warn("BookingPage: Missing essential info (screeningInfo, movieInfo, user). Navigating away.");
            navigate(movieInfo?.id ? `/movie/${movieInfo.id}` : '/');
            return;
        }

        const fetchPageData = async () => {
            // Tải sơ đồ ghế
            setLoadingSeats(true);
            setErrorSeats('');
            try {
                const seatsResponse = await getSeatStatus(screeningInfo.screeningId);
                if (seatsResponse.data && seatsResponse.data.result) {
                    setSeats(seatsResponse.data.result);
                } else {
                     setErrorSeats('Không tải được sơ đồ ghế (dữ liệu không hợp lệ).');
                }
            } catch (err) {
                console.error("Error fetching seats:", err);
                setErrorSeats(err.response?.data?.message || 'Không thể tải sơ đồ ghế.');
            } finally {
                setLoadingSeats(false);
            }

            // Tải phương thức thanh toán
            setLoadingPaymentMethods(true);
            try {
                const paymentResponse = await getActivePaymentMethods();
                if (paymentResponse.data && paymentResponse.data.result) {
                    setPaymentMethods(paymentResponse.data.result);
                    if (paymentResponse.data.result.length > 0) {
                        setSelectedPaymentMethodId(paymentResponse.data.result[0].id.toString());
                    }
                }
            } catch (err) {
                console.error("Error fetching payment methods:", err);
                // Có thể set lỗi cho payment methods nếu cần
            } finally {
                setLoadingPaymentMethods(false);
            }
        };

        fetchPageData();
    }, [screeningInfo, movieInfo, user, navigate]);


    useEffect(() => {
        if (!screeningInfo || !screeningInfo.fareType) {
            setTotalPrice(0);
            setFinalPrice(0);
            return;
        }

        let currentTotal = 0;
        const baseTicketPrice = parseFloat(screeningInfo.fareType.basePrice || 0);

        selectedSeats.forEach(seat => {
            // seat.seatTypePrice là phụ thu của loại ghế đã được lấy khi chọn ghế
            currentTotal += baseTicketPrice + parseFloat(seat.seatTypePrice || 0);
        });
        setTotalPrice(currentTotal);

        let currentDiscount = 0;
        if (appliedPromotion && currentTotal > 0) {
            if (currentTotal < parseFloat(appliedPromotion.minOrder)) {
                setPromotionError(`Tổng tiền ${currentTotal.toLocaleString('vi-VN')}đ không đủ điều kiện tối thiểu ${parseFloat(appliedPromotion.minOrder).toLocaleString('vi-VN')}đ của mã khuyến mãi.`);
                setAppliedPromotion(null);
                setPromotionSuccess('');
            } else {
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
                setPromotionError('');
            }
        } else if (appliedPromotion && currentTotal === 0 && selectedSeats.length === 0) {
            // Nếu không còn ghế nào được chọn, reset KM
            setAppliedPromotion(null);
            setPromotionSuccess('');
            setPromotionError('');
        }

        setDiscountAmount(currentDiscount);
        setFinalPrice(Math.max(0, currentTotal - currentDiscount)); // Đảm bảo giá không âm

    }, [selectedSeats, appliedPromotion, screeningInfo]);

    const handleSeatSelect = (seatFromMap) => { // seatFromMap là object từ SeatSelection (API getSeatStatus)
        setSelectedSeats(prevSeats => {
            const isSelected = prevSeats.find(s => s.seatId === seatFromMap.seatId);
            if (isSelected) {
                return prevSeats.filter(s => s.seatId !== seatFromMap.seatId);
            } else {
                // Lưu các thông tin cần thiết của ghế đã chọn
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
                    setPromotionError(`Tổng tiền hiện tại ${totalPrice.toLocaleString('vi-VN')}đ chưa đạt mức tối thiểu ${parseFloat(promoData.minOrder).toLocaleString('vi-VN')}đ của khuyến mãi.`);
                    return;
                }
                setAppliedPromotion(promoData);
                setPromotionSuccess('Áp dụng mã khuyến mãi thành công!');
            } else {
                setPromotionError(response.data?.message || 'Mã khuyến mãi không hợp lệ hoặc đã hết hạn.');
            }
        } catch (err) {
            console.error("Error applying promotion:", err);
            setPromotionError(err.response?.data?.message || 'Lỗi khi áp dụng mã khuyến mãi.');
        } finally {
            setCheckingPromotion(false);
        }
    };

     const handleSubmitBooking = async () => {
        if (selectedSeats.length === 0) {
            alert('Vui lòng chọn ít nhất một ghế.');
            return;
        }
        if (!selectedPaymentMethodId) {
            alert('Vui lòng chọn phương thức thanh toán.');
            return;
        }
        if (!screeningInfo || !screeningInfo.screeningId) {
            alert('Thiếu thông tin suất chiếu. Vui lòng thử lại.');
            return;
        }

        setIsSubmitting(true);
        const bookingPayload = {
            screeningId: screeningInfo.screeningId,
            seatIds: selectedSeats.map(s => s.seatId),
            promotionCode: appliedPromotion ? appliedPromotion.code : null,
            paymentMethodId: parseInt(selectedPaymentMethodId),
        };

        try {
            console.log('BookingPage - Submitting booking payload:', bookingPayload);
            const response = await createBooking(bookingPayload);
            console.log('BookingPage - Create booking API raw response:', response);

            if (response && response.data) {
                console.log('BookingPage - Create booking response.data:', response.data);
                const bookingResult = response.data.result;

                if (response.data.status === 201 && bookingResult) {
                    if (bookingResult.paymentUrl) {
                        console.log('BookingPage - Redirecting to payment URL:', bookingResult.paymentUrl);

                        // LƯU movieInfo VÀO LOCALSTORAGE
                        if (movieInfo) { // Đảm bảo movieInfo tồn tại
                            try {
                                localStorage.setItem('lastMovieInfoForBooking', JSON.stringify(movieInfo));
                                console.log('BookingPage - Saved movieInfo to localStorage:', movieInfo);
                            } catch (e) {
                                console.error('BookingPage - Error saving movieInfo to localStorage:', e);
                                // Có thể lưu chỉ movieId nếu stringify lỗi (movieInfo quá lớn hoặc có circular reference)
                                if (movieInfo.id) {
                                     localStorage.setItem('lastMovieIdForBooking', movieInfo.id.toString());
                                }
                            }
                        }
                        // Optional: Lưu bookingId nếu cần tham chiếu sau khi quay lại từ VNPAY
                        if (bookingResult.bookingId) {
                            localStorage.setItem('lastVnpayBookingId', bookingResult.bookingId.toString());
                        }

                        window.location.href = bookingResult.paymentUrl;
                    } else {
                        console.log('BookingPage - Navigating to success page with bookingId:', bookingResult.bookingId);
                        navigate('/booking/success', { state: { bookingId: bookingResult.bookingId } });
                    }
                } else {
                    console.error('BookingPage - Booking creation failed or API status not 201:', response.data);
                    alert(response.data.message || 'Đặt vé không thành công. Vui lòng thử lại.');
                }
            } else {
                console.error('BookingPage - Invalid API response structure (no data):', response);
                alert('Không nhận được phản hồi hợp lệ từ máy chủ. Vui lòng thử lại.');
            }
        } catch (err) {
            console.error("Error creating booking (catch block):", err);
            alert(err.response?.data?.message || err.message || 'Có lỗi xảy ra trong quá trình đặt vé.');
        } finally {
            setIsSubmitting(false);
        }
    };


    // Hàm format thời gian chiếu, nhận vào ISO string từ screeningInfo.showDateTime
    const formatScreeningTime = (isoDateTimeString) => {
        if (!isoDateTimeString) {
             // Fallback nếu screeningInfo.showDateTime không được truyền đúng cách
            if (screeningInfo && screeningInfo.time && screeningInfo.showDateTimeFromMovieDetail) {
                 try {
                    // showDateTimeFromMovieDetail có dạng "YYYY-MM-DDTHH:mm:ss" (được ghép từ BookingSchedule)
                    // Chúng ta có thể parse trực tiếp nó
                    return format(parseISO(screeningInfo.showDateTimeFromMovieDetail), 'HH:mm - EEEE, dd/MM/yyyy', { locale: vi });
                } catch (error) {
                     console.error("Error formatting fallback screening time:", error, screeningInfo);
                    return "N/A (thời gian không hợp lệ)";
                }
            }
            console.warn("formatScreeningTime called with invalid dateTimeString:", isoDateTimeString, "Full screeningInfo:", screeningInfo);
            return "N/A";
        }
        try {
            return format(parseISO(isoDateTimeString), 'HH:mm - EEEE, dd/MM/yyyy', { locale: vi });
        } catch (error) {
            console.error("Error formatting screening time from ISO string:", error, isoDateTimeString);
            return `Lỗi format: ${isoDateTimeString}`;
        }
    };


    if (!screeningInfo || !movieInfo || !user) {
        // Điều kiện này đã được xử lý ở useEffect đầu tiên, nhưng thêm ở đây để chắc chắn
        return <div className="container mt-5 text-center"><p>Đang chuyển hướng hoặc thiếu thông tin...</p></div>;
    }

    return (
        <div className={cx('booking-container', 'container', 'mt-4', 'mb-5')}>
            <h2 className={cx('page-title')}>Đặt Vé Xem Phim</h2>

            <div className="row">
                <div className="col-lg-8">
                    <div className={cx('section', 'movie-info-section')}>
                        <h4>{movieInfo?.nameVN || 'Tên phim'}</h4>
                        <p><i className="fas fa-calendar-alt"></i> Suất chiếu: {screeningInfo ? formatScreeningTime(screeningInfo.showDateTime) : 'N/A'}</p>
                        <p><i className="fas fa-video"></i> Phòng chiếu: {screeningInfo?.cinemaRoom?.cinemaRoomName || 'N/A'}</p>
                        <p><i className="fas fa-film"></i> Định dạng: {screeningInfo?.movieFormat || 'N/A'}</p>
                    </div>

                    <div className={cx('section', 'seat-selection-section')}>
                        <h4>Chọn ghế</h4>
                        {loadingSeats && <div className="text-center my-3"><p>Đang tải sơ đồ ghế...</p></div>}
                        {errorSeats && <p className="text-danger text-center my-3">{errorSeats}</p>}
                        {!loadingSeats && !errorSeats && seats.length > 0 && screeningInfo?.fareType ? (
                            <SeatSelection
                                seatsData={seats}
                                selectedSeats={selectedSeats}
                                onSeatSelect={handleSeatSelect}
                                baseTicketPrice={parseFloat(screeningInfo.fareType.basePrice || 0)}
                            />
                        ) : null}
                        {!loadingSeats && !errorSeats && seats.length === 0 && (
                             <p className="text-center my-3">Không có thông tin ghế cho suất chiếu này.</p>
                        )}
                         {!loadingSeats && !errorSeats && !screeningInfo?.fareType && ( // Kiểm tra thêm !screeningInfo?.fareType
                             <p className="text-center my-3">Không có thông tin giá vé cho suất chiếu này.</p>
                        )}
                    </div>
                </div>

                <div className="col-lg-4">
                     {screeningInfo?.fareType ? (
                        <OrderSummary
                            movieInfo={movieInfo}
                            screeningInfo={screeningInfo}
                            selectedSeats={selectedSeats}
                            totalPrice={totalPrice}
                            discountAmount={discountAmount}
                            finalPrice={finalPrice}
                            baseTicketPrice={parseFloat(screeningInfo.fareType.basePrice || 0)}
                        />
                    ) : (
                        <div className={cx('section', 'mt-3', 'text-center')}>
                            <p>Không thể tính toán đơn hàng do thiếu thông tin giá vé.</p>
                        </div>
                    )}

                    <div className={cx('section', 'promotion-section', 'mt-3')}>
                        <h4>Mã khuyến mãi</h4>
                        <div className="input-group mb-2">
                            <input
                                type="text"
                                className="form-control"
                                placeholder="Nhập mã giảm giá"
                                value={promotionCode}
                                onChange={(e) => setPromotionCode(e.target.value)}
                                disabled={checkingPromotion || totalPrice === 0} // Disable nếu không có gì để áp dụng KM
                            />
                            <CustomizeButton // Hoặc <button>
                                primary
                                onClick={handleApplyPromotion}
                                disabled={checkingPromotion || !promotionCode.trim() || totalPrice === 0}
                                className={cx('btn-apply-promo')}
                            >
                                {checkingPromotion ? 'Đang kiểm tra...' : 'Áp dụng'}
                            </CustomizeButton>
                        </div>
                        {promotionError && <p className="text-danger small mt-1">{promotionError}</p>}
                        {promotionSuccess && <p className="text-success small mt-1">{promotionSuccess}</p>}
                         {appliedPromotion && (
                            <p className="small text-muted mt-1">
                                Đã áp dụng: {appliedPromotion.code}
                            </p>
                        )}
                    </div>

                    <div className={cx('section', 'payment-section', 'mt-3')}>
                        <h4>Chọn phương thức thanh toán</h4>
                        {loadingPaymentMethods && <p className="text-center my-2">Đang tải...</p>}
                        {!loadingPaymentMethods && paymentMethods.map(method => (
                            <div className="form-check" key={method.id}>
                                <input
                                    className="form-check-input"
                                    type="radio"
                                    name="paymentMethod"
                                    id={`paymentMethod-${method.id}`}
                                    value={method.id.toString()}
                                    checked={selectedPaymentMethodId === method.id.toString()}
                                    onChange={(e) => setSelectedPaymentMethodId(e.target.value)}
                                />
                                <label className="form-check-label" htmlFor={`paymentMethod-${method.id}`}>
                                    {method.name}
                                </label>
                            </div>
                        ))}
                         {!loadingPaymentMethods && paymentMethods.length === 0 && (
                            <p className="text-center my-2">Không có phương thức thanh toán nào.</p>
                        )}
                    </div>

                    <CustomizeButton // Hoặc <button>
                        gold
                        large
                        block
                        onClick={handleSubmitBooking}
                        disabled={
                            isSubmitting ||
                            selectedSeats.length === 0 ||
                            !selectedPaymentMethodId ||
                            !screeningInfo?.fareType || // Phải có giá vé
                            finalPrice < 0 // Tổng tiền cuối không thể âm
                        }
                        className={cx('btn-submit-booking', 'mt-4')}
                    >
                        {isSubmitting ? 'Đang xử lý...' : `Thanh Toán ${finalPrice > 0 ? finalPrice.toLocaleString('vi-VN') + 'đ' : ''}`}
                    </CustomizeButton>
                </div>
            </div>
        </div>
    );
};

export default BookingPage;