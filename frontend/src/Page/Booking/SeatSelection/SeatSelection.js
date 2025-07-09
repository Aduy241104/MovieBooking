import React from 'react';
import classNames from 'classnames/bind';
import styles from './seatSelection.module.scss';

const cx = classNames.bind(styles);

const SeatSelection = ({ seatsData, selectedSeats, onSeatSelect, screeningInfo }) => {
    // Nhóm ghế theo hàng và sắp xếp (giữ nguyên)
    const seatsByRow = (seatsData || []).reduce((acc, seat) => {
        const row = seat.seatRow;
        if (!acc[row]) acc[row] = [];
        acc[row].push(seat);
        acc[row].sort((a, b) => parseInt(a.seatCol) - parseInt(b.seatCol));
        return acc;
    }, {});
    const sortedRows = Object.keys(seatsByRow).sort();

    // Hàm onClick đã được đơn giản hóa
    const handleSeatClick = (clickedSeat) => {
        // Chỉ cần gọi hàm prop được truyền từ BookingPage
        onSeatSelect(clickedSeat);
    };

    return (
        <div className={cx('seat-map-container')}>
            <div className={cx('screen')}>MÀN HÌNH</div>
            <div className={cx('seat-area')}>
                {sortedRows.map(rowLabel => (
                    <div key={rowLabel} className={cx('seat-row')}>
                        <div className={cx('row-label')}>{rowLabel}</div>
                        <div className={cx('seats-in-row')}>
                            {seatsByRow[rowLabel].map(seat => {
                                const isSelected = selectedSeats.some(s => s.seatId === seat.seatId);
                                const isDisabled = seat.status === 'Booked' || seat.status === 'Unavailable';
                                const seatTypeClass = seat.seatTypeName ? seat.seatTypeName.toLowerCase() : 'regular';
                                const seatClass = cx('seat', seatTypeClass, { 'selected': isSelected, 'disabled': isDisabled });

                                return (
                                    <div
                                        key={seat.seatId}
                                        className={seatClass}
                                        onClick={() => !isDisabled && handleSeatClick(seat)} // Gọi hàm onClick đơn giản
                                        title={`${seat.seatTypeName}`}
                                    >
                                        {`${seat.seatRow}${seat.seatCol}`}
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                ))}
            </div>
            {/* Phần legend giữ nguyên */}
            <div className={cx('legend', 'mt-4')}>
                {/* ... */}
            </div>
        </div>
    );
};

export default SeatSelection;