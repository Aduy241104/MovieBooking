import React, { useState, useEffect } from 'react';
import { differenceInSeconds } from 'date-fns';

const CountdownTimer = ({ expiryTime, onExpire }) => {
    const [timeLeft, setTimeLeft] = useState(() => differenceInSeconds(new Date(expiryTime), new Date()));

    useEffect(() => {
        if (timeLeft <= 0) {
            if (onExpire) onExpire();
            return;
        }

        const intervalId = setInterval(() => {
            setTimeLeft(prevTime => prevTime - 1);
        }, 1000);

        return () => clearInterval(intervalId);
    }, [timeLeft, onExpire]);

    if (timeLeft <= 0) {
        return <span className="text-danger">Đã hết hạn</span>;
    }

    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;

    return (
        <span className="text-warning">
            Hết hạn trong: {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
        </span>
    );
};

export default CountdownTimer;