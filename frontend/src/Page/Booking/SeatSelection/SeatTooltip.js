import React from 'react';
import CountdownTimer from '../../../components/Booking/CountdownTimer'; // Điều chỉnh đường dẫn đến CountdownTimer của bạn

const SeatTooltip = ({ seat, isSelected, getSeatDisplayPrice, onExpire }) => {
    if (seat.status === 'Booked') return 'Ghế đã được đặt';
    if (seat.status === 'Unavailable') return 'Ghế không khả dụng';
    if (isSelected) return 'Nhấn để bỏ chọn ghế này';

    if (seat.status === 'Pending') {
        return (
            <div>
                <div>Ghế đang được giữ</div>
                <CountdownTimer expiryTime={seat.expiresAt} onExpire={onExpire} />
            </div>
        );
    }
    
    return (
        <div>
            <div>{seat.seatTypeName}</div>
            <strong>{getSeatDisplayPrice(seat).toLocaleString('vi-VN')}đ</strong>
        </div>
    );
};

export default SeatTooltip;