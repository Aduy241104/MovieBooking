import React from 'react';
import styles from './orderSummary.module.scss';
import classNames from 'classnames/bind';
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';

const cx = classNames.bind(styles);

const OrderSummary = ({ movieInfo, screeningInfo, selectedSeats, totalPrice, discountAmount, finalPrice, baseTicketPrice }) => {

    const formatScreeningDateTime = () => {
        if (!screeningInfo || !screeningInfo.showDateTime) return 'N/A';
        try {
            // screeningInfo.showDateTime là LocalDateTime từ backend, đã đúng chuẩn ISO
            return format(parseISO(screeningInfo.showDateTime), 'HH:mm - EEEE, dd/MM/yyyy', { locale: vi });
        } catch (e) {
            console.error("Error formatting screening date:", e, screeningInfo.showDateTime);
            return 'N/A';
        }
    };

    return (
        <div className={cx('order-summary-card', 'p-3', 'border', 'rounded')}>
            <h4 className={cx('summary-title', 'mb-3')}>Tóm tắt đơn hàng</h4>

            <div className={cx('movie-details', 'mb-3')}>
                <h5 className={cx('movie-name')}>{movieInfo.nameVN}</h5>
                <p className={cx('screening-time')}>
                    <i className="far fa-clock"></i> {formatScreeningDateTime()}
                </p>
                <p className={cx('cinema-info')}>
                    <i className="fas fa-map-marker-alt"></i> {screeningInfo.cinemaRoom.cinemaRoomName} - {screeningInfo.movieFormat}
                </p>
            </div>

            <div className={cx('selected-seats-info', 'mb-3')}>
                <h6>Ghế đã chọn ({selectedSeats.length}):</h6>
                {selectedSeats.length > 0 ? (
                    <ul className={cx('seat-list', 'list-unstyled')}>
                        {selectedSeats.map(seat => (
                            <li key={seat.seatId} className={cx('seat-item')}>
                                <span>Ghế {seat.seatRow}{seat.seatCol} ({seat.seatTypeName})</span>
                                <span>{(baseTicketPrice + seat.seatTypePrice).toLocaleString('vi-VN')}đ</span>
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p className="text-muted">Chưa chọn ghế nào.</p>
                )}
            </div>

            <div className={cx('pricing-details')}>
                <div className={cx('price-row')}>
                    <span>Tạm tính:</span>
                    <span className={cx('amount')}>{totalPrice.toLocaleString('vi-VN')}đ</span>
                </div>
                {discountAmount > 0 && (
                    <div className={cx('price-row', 'discount')}>
                        <span>Giảm giá:</span>
                        <span className={cx('amount')}>- {discountAmount.toLocaleString('vi-VN')}đ</span>
                    </div>
                )}
                <hr className={cx('divider')} />
                <div className={cx('price-row', 'total')}>
                    <h6>Tổng cộng:</h6>
                    <h6 className={cx('amount')}>{finalPrice.toLocaleString('vi-VN')}đ</h6>
                </div>
            </div>
        </div>
    );
};

export default OrderSummary;