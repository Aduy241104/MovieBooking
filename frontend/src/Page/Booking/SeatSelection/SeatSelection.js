import React from 'react';
import styles from './seatSelection.module.scss';
import classNames from 'classnames/bind';

const cx = classNames.bind(styles);

const SeatSelection = ({ seatsData, selectedSeats, onSeatSelect, baseTicketPrice }) => {
    // Nhóm ghế theo hàng
    const seatsByRow = seatsData.reduce((acc, seat) => {
        const row = seat.seatRow;
        if (!acc[row]) {
            acc[row] = [];
        }
        acc[row].push(seat);
        // Sắp xếp ghế trong hàng theo cột (nếu cột là số)
        acc[row].sort((a, b) => parseInt(a.seatCol) - parseInt(b.seatCol));
        return acc;
    }, {});

    // Sắp xếp các hàng (A, B, C,...)
    const sortedRows = Object.keys(seatsByRow).sort();

    const getSeatDisplayPrice = (seat) => {
        return baseTicketPrice + (parseFloat(seat.seatTypePrice) || 0);
    };


    return (
        <div className={cx('seat-map-container')}>
            <div className={cx('screen')}>MÀN HÌNH</div>
            {sortedRows.map(rowLabel => (
                <div key={rowLabel} className={cx('seat-row')}>
                    <div className={cx('row-label')}>{rowLabel}</div>
                    <div className={cx('seats-in-row')}>
                        {seatsByRow[rowLabel].map(seat => {
                            const isSelected = selectedSeats.some(s => s.seatId === seat.seatId);
                            const isDisabled = seat.status === 'Booked' || seat.status === 'Unavailable';
                            let seatClass = cx('seat', seat.seatTypeName.toLowerCase().replace(' ', '-')); // vd: ghe-vip

                            if (isSelected) {
                                seatClass = cx(seatClass, 'selected');
                            }
                            if (isDisabled) {
                                seatClass = cx(seatClass, 'disabled');
                            }

                            return (
                                <div
                                    key={seat.seatId}
                                    className={seatClass}
                                    onClick={() => !isDisabled && onSeatSelect(seat)}
                                    title={
                                        isDisabled
                                        ? (seat.status === 'Booked' ? 'Ghế đã được đặt' : 'Ghế không khả dụng')
                                        : `${seat.seatTypeName}: ${getSeatDisplayPrice(seat).toLocaleString('vi-VN')}đ`
                                    }
                                >
                                    {seat.seatCol}
                                </div>
                            );
                        })}
                    </div>
                </div>
            ))}

            <div className={cx('legend', 'mt-3')}>
                <div className={cx('legend-item')}>
                    <div className={cx('seat', 'available-legend')}></div>
                    <span>Ghế trống</span>
                </div>
                <div className={cx('legend-item')}>
                    <div className={cx('seat', 'ghe-vip', 'available-legend')}></div> {/* Ví dụ cho VIP */}
                    <span>Ghế VIP trống</span>
                </div>
                 <div className={cx('legend-item')}>
                    <div className={cx('seat', 'ghe-doi', 'available-legend')}></div> {/* Ví dụ cho đôi */}
                    <span>Ghế đôi trống</span>
                </div>
                <div className={cx('legend-item')}>
                    <div className={cx('seat', 'selected')}></div>
                    <span>Ghế đang chọn</span>
                </div>
                <div className={cx('legend-item')}>
                    <div className={cx('seat', 'disabled')}></div>
                    <span>Ghế đã đặt/Không khả dụng</span>
                </div>
            </div>
        </div>
    );
};

export default SeatSelection;