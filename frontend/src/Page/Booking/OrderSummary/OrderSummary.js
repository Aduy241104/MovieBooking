import React from 'react';
import styles from './orderSummary.module.scss';
import classNames from 'classnames/bind';
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';

const cx = classNames.bind(styles);

const OrderSummary = ({ movieInfo, screeningInfo, selectedSeats, totalPrice, discountAmount, pointsDiscount, finalPrice }) => {
    // ... formatScreeningDateTime giữ nguyên ...

    return (
        <div className={cx('order-summary-card', 'p-3', 'border', 'rounded')}>
            {/* ... Phần thông tin phim và ghế đã chọn giữ nguyên ... */}

            <div className={cx('pricing-details')}>
                <div className={cx('price-row')}>
                    <span>Tạm tính:</span>
                    <span className={cx('amount')}>{totalPrice.toLocaleString('vi-VN')}đ</span>
                </div>
                {discountAmount > 0 && (
                    <div className={cx('price-row', 'discount')}>
                        <span>Giảm giá (Khuyến mãi):</span>
                        <span className={cx('amount')}>- {discountAmount.toLocaleString('vi-VN')}đ</span>
                    </div>
                )}
                {/* <<< THÊM DÒNG NÀY >>> */}
                {pointsDiscount > 0 && (
                    <div className={cx('price-row', 'discount')}>
                        <span>Giảm giá (từ điểm):</span>
                        <span className={cx('amount')}>- {pointsDiscount.toLocaleString('vi-VN')}đ</span>
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