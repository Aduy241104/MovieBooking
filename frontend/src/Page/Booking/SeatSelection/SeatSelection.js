import React from 'react';
import { Tooltip } from 'react-tooltip';
import styles from './seatSelection.module.scss';
import classNames from 'classnames/bind';
import SeatTooltip from './SeatTooltip';

const cx = classNames.bind(styles);

// <<< BỎ HOÀN TOÀN OBJECT SEAT_COLORS Ở ĐÂY >>>

const SeatSelection = ({ seatsData, selectedSeats, onSeatSelect, screeningInfo, onSeatExpire }) => {
    const seatsByRow = (seatsData || []).reduce((acc, seat) => {
        const row = seat.seatRow;
        if (!acc[row]) acc[row] = [];
        acc[row].push(seat);
        acc[row].sort((a, b) => parseInt(a.seatCol) - parseInt(b.seatCol));
        return acc;
    }, {});
    const sortedRows = Object.keys(seatsByRow).sort();

    const getSeatDisplayPrice = (seat) => {
        // ... logic tính giá của bạn giữ nguyên ...
        if (!screeningInfo || !screeningInfo.fareType) return 0;
        const basePrice = parseFloat(screeningInfo.fareType.basePrice || 0);
        const movieFormat = screeningInfo.fareType.movieFormat || '';
        const timeSlotType = screeningInfo.fareType.timeSlotType || '';
        let formatSurcharge = 0;
        if (movieFormat.toUpperCase() === '3D') formatSurcharge = 20000;
        else if (movieFormat.toUpperCase() === 'IMAX') formatSurcharge = 35000;
        let timeSurcharge = 0;
        if (timeSlotType === 'Cuối Tuần') timeSurcharge = 10000;
        else if (timeSlotType === 'Ngày Lễ') timeSurcharge = 15000;
        const seatTypePrice = parseFloat(seat.seatTypePrice || 0);
        return basePrice + formatSurcharge + timeSurcharge + seatTypePrice;
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
                                const isDisabled = seat.status === 'Booked' || seat.status === 'Unavailable' || seat.status === 'Pending';
                                
                                // Tạo class loại ghế từ seatTypeName
                                const seatTypeClass = seat.seatTypeName ? seat.seatTypeName.toLowerCase() : 'regular';

                                // Gộp tất cả các class cần thiết
                                const seatClasses = cx('seat', seatTypeClass, {
                                    'selected': isSelected,
                                    'disabled': isDisabled,
                                    'pending': seat.status === 'Pending',
                                });
                                
                                const seatTooltipId = `seat-tooltip-${seat.seatId}`;

                                return (
                                    <div 
                                        key={seat.seatId} 
                                        id={seatTooltipId} 
                                        className={seatClasses} // <<< CHỈ DÙNG CLASSNAME >>>
                                        // Bỏ hoàn toàn prop `style`
                                        onClick={() => !isDisabled && onSeatSelect(seat)}
                                    >
                                        {`${seat.seatRow}${seat.seatCol}`}
                                        <Tooltip anchorId={seatTooltipId} place="top" effect="solid" className={cx('custom-tooltip')}>
                                            <SeatTooltip seat={seat} isSelected={isSelected} getSeatDisplayPrice={getSeatDisplayPrice} onExpire={onSeatExpire} />
                                        </Tooltip>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                ))}
            </div>
            {/* <<< SỬA LẠI LEGEND ĐỂ DÙNG CLASS THAY VÌ INLINE STYLE >>> */}
            <div className={cx('legend', 'mt-4')}>
                <div className={cx('legend-item')}>
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
                    <div className={cx('seat', 'pending')}></div>
                    <span>Ghế đang giữ</span>
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