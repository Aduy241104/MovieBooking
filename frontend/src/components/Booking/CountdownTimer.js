import React, { useState, useEffect } from 'react';
import { differenceInSeconds, isAfter } from 'date-fns';

const CountdownTimer = ({ expiryTime, onExpire }) => {
    // Hàm tính toán thời gian còn lại
    const calculateTimeLeft = () => {
        const target = new Date(expiryTime);
        // Nếu thời gian hết hạn không hợp lệ, trả về 0
        if (isNaN(target.getTime())) {
            return 0;
        }
        return differenceInSeconds(target, new Date());
    };

    const [timeLeft, setTimeLeft] = useState(calculateTimeLeft);

    useEffect(() => {
        // Nếu ngay từ đầu đã hết giờ, gọi onExpire và không làm gì thêm
        if (timeLeft <= 0) {
            if (onExpire) {
                onExpire();
            }
            return;
        }

        // Nếu còn thời gian, bắt đầu đếm ngược
        const intervalId = setInterval(() => {
            setTimeLeft(prevTime => {
                if (prevTime <= 1) { // Khi chỉ còn 1 giây
                    clearInterval(intervalId); // Dừng interval
                    if (onExpire) {
                        onExpire(); // Gọi onExpire
                    }
                    return 0;
                }
                return prevTime - 1;
            });
        }, 1000);

        // Dọn dẹp interval khi component unmount
        return () => clearInterval(intervalId);
    }, [expiryTime, onExpire]); // Chạy lại hiệu ứng nếu expiryTime thay đổi

    if (timeLeft <= 0) {
        return <span className="text-danger">Đã hết hạn</span>;
    }

    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;

    return (
        <span className="text-warning" style={{ fontSize: '0.8rem' }}>
            Hết hạn sau: {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
        </span>
    );
};

export default CountdownTimer;