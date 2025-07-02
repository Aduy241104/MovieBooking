import React from 'react';
import styles from './seatSelection.module.scss';
import classNames from 'classnames/bind';

const cx = classNames.bind(styles);

const SeatSelection = ({ seatsData, selectedSeats, onSeatSelect, baseTicketPrice }) => {
    // Nhóm ghế theo hàng
    const seatsByRow = (seatsData || []).reduce((acc, seat) => {
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
        // baseTicketPrice được truyền từ BookingPage, cần logic tính giá đầy đủ ở đó
        // Ở đây chỉ là ví dụ cơ bản
        return (baseTicketPrice || 0) + (parseFloat(seat.seatTypePrice) || 0);
    };


    return (
        <div className={cx('seat-map-container')}>
            <div className={cx('screen')}>MÀN HÌNH</div>
            <div className={cx('seat-area')}> {/* Thêm seat-area để dễ căn giữa */}
                {sortedRows.map(rowLabel => (
                    <div key={rowLabel} className={cx('seat-row')}>
                        <div className={cx('row-label')}>{rowLabel}</div>
                        <div className={cx('seats-in-row')}>
                            {seatsByRow[rowLabel].map(seat => {
                                const isSelected = selectedSeats.some(s => s.seatId === seat.seatId);
                                const isDisabled = seat.status === 'Booked' || seat.status === 'Unavailable';
                                
                                // Tạo class CSS từ seatTypeName, ví dụ: "regular", "vip", "couple"
                                const seatTypeClass = seat.seatTypeName ? seat.seatTypeName.toLowerCase() : 'regular';

                                const seatClass = cx('seat', seatTypeClass, {
                                    'selected': isSelected,
                                    'disabled': isDisabled,
                                });

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
                                        {/* --- THAY ĐỔI 1: HIỂN THỊ TỌA ĐỘ ĐẦY ĐỦ --- */}
                                        {`${seat.seatRow}${seat.seatCol}`}
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                ))}
            </div>

            {/* --- THAY ĐỔI 2: SỬA LẠI LEGEND --- */}
            <div className={cx('legend', 'mt-4')}>
                <div className={cx('legend-item')}>
                    {/* Sử dụng class "regular" để khớp với logic */}
                    <div className={cx('seat', 'regular')}></div>
                    <span>Ghế thường</span>
                </div>
                <div className={cx('legend-item')}>
                    <div className={cx('seat', 'vip')}></div>
                    <span>Ghế VIP</span>
                </div>
                 <div className={cx('legend-item')}>
                    <div className={cx('seat', 'couple')}></div>
                    <span>Ghế đôi</span>
                </div>
                <div className={cx('legend-item')}>
                    <div className={cx('seat', 'selected')}></div>
                    <span>Ghế đang chọn</span>
                </div>
                <div className={cx('legend-item')}>
                    <div className={cx('seat', 'disabled')}></div>
                    <span>Ghế đã đặt</span>
                </div>
            </div>
        </div>
    );
};

export default SeatSelection;