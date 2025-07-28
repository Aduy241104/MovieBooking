import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMovieSchedules } from '../../../service/ScreeningService'; 
import { format, parseISO, addDays, isSameDay, startOfDay } from 'date-fns';
import { vi } from 'date-fns/locale';
import { AuthContext } from '../../../context/AuthContext'; 
import styles from './bookingSchedule.module.scss';
import classNames from 'classnames/bind';

const cx = classNames.bind(styles);

const BookingSchedule = ({ movie }) => {
 // schedulesFromApi: Original data from API { "YYYY-MM-DD": [showtime], ... }
    const [schedulesFromApi, setSchedulesFromApi] = useState(null);
    const [loadingSchedules, setLoadingSchedules] = useState(false);
    const [errorSchedules, setErrorSchedules] = useState('');

    // selectedDate: Date object
    const [selectedDate, setSelectedDate] = useState(startOfDay(new Date())); // Default select today

    const navigate = useNavigate();
    const { user } = useContext(AuthContext);

    // Create a list of 8 days to display, starting from today
    const displayDays = Array.from({ length: 8 }, (_, i) => addDays(startOfDay(new Date()), i));

    useEffect(() => {
        console.log('BookingSchedule.js - useEffect - movie prop:', movie);
        if (movie && movie.id) {
            const fetchSchedules = async () => {
                setLoadingSchedules(true);
                setErrorSchedules('');
                setSchedulesFromApi(null); 
                try {
                    const response = await getMovieSchedules(movie.id);
                    console.log('BookingSchedule.js - API getMovieSchedules FULL response.data:', response.data);

                    if (response.data && response.data.result && response.data.result.schedules) {
                        const fetchedApiSchedules = response.data.result.schedules;
                        console.log('BookingSchedule.js - Value of response.data.result.schedules (fetchedApiSchedules):', fetchedApiSchedules);
                        setSchedulesFromApi(fetchedApiSchedules);
                        const currentSelectedDateStr = format(selectedDate, 'yyyy-MM-dd');
                        if (!fetchedApiSchedules[currentSelectedDateStr]) {
                            const apiDatesWithSchedules = Object.keys(fetchedApiSchedules).filter(dateStr => fetchedApiSchedules[dateStr]?.length > 0).sort();
                            if (apiDatesWithSchedules.length > 0) {
                               // Automatically jump to the first day that HAS an actual showtime
                                 setSelectedDate(startOfDay(parseISO(apiDatesWithSchedules[0])));
                            }
                        }

                    } else {
                        console.log('BookingSchedule.js - API response does not have expected structure or schedules is missing. Response result:', response.data?.result);
                        setSchedulesFromApi({}); 
                    }
                } catch (err) {
                    console.error("Error fetching schedules:", err);
                    if (err.response) {
                        console.error("Error response data:", err.response.data);
                        console.error("Error response status:", err.response.status);
                    }
                    setErrorSchedules(err.response?.data?.message || 'Không thể tải lịch chiếu.');
                    setSchedulesFromApi({});
                } finally {
                    setLoadingSchedules(false);
                }
            };
            fetchSchedules();
        } else {
            setSchedulesFromApi(null); 
        }
    }, [movie, selectedDate]); 

    const handleDateSelect = (dateObject) => {
        console.log('BookingSchedule.js - Date selected by user (object):', dateObject);
        setSelectedDate(dateObject);
    };

    const handleTimeSelect = (screeningInfo) => {
        if (!user) {
            alert("Vui lòng đăng nhập để đặt vé.");
            navigate('/login', { state: { from: movie ? `/movie/${movie.id}` : '/' } });
            return;
        }

        const [hours, minutes, seconds] = screeningInfo.time.split(':').map(Number);
        const showDateTimeObject = new Date(selectedDate);
        showDateTimeObject.setHours(hours, minutes, seconds || 0, 0);

        const fullScreeningInfo = {
            ...screeningInfo,
            showDateTime: showDateTimeObject.toISOString(), // Keep showDateTime as ISO string for booking page
        };
        console.log('BookingSchedule.js - Navigating to booking with screeningInfo:', fullScreeningInfo, 'and movieInfo:', movie);
        navigate('/booking', { state: { screeningInfo: fullScreeningInfo, movieInfo: movie } });
    };

    const formatTimeForDisplay = (timeStr) => { //String "HH:mm:ss"
        if (typeof timeStr === 'string') {
            const parts = timeStr.split(':');
            if (parts.length >= 2) {
                return `${parts[0]}:${parts[1]}`; //HH:mm
            }
            return timeStr;
        }
        console.warn('Unexpected timeObj format for formatTimeForDisplay:', timeStr);
        return 'N/A';
    };


    const getEndTime = (startTimeStr, durationMinutes) => {
        if (!startTimeStr || typeof durationMinutes !== 'number' || isNaN(durationMinutes)) return '';
        try {
            const [hours, minutes] = startTimeStr.split(':').map(Number);
            const startDate = new Date(selectedDate); 
            startDate.setHours(hours, minutes, 0, 0);

            const endDate = new Date(startDate.getTime() + durationMinutes * 60000); // Duration (ms)
            return format(endDate, 'HH:mm');
        } catch (e) {
            console.error('Error calculating end time:', e);
            return '';
        }
    };

    // Get showtimes for the selected date (selectedDate)
    const selectedDateString = selectedDate ? format(selectedDate, 'yyyy-MM-dd') : null;
    const screeningsForSelectedDate = schedulesFromApi && selectedDateString
        ? schedulesFromApi[selectedDateString] || []
        : [];

    console.log(`BookingSchedule.js - Render - selectedDate: ${selectedDateString}, screeningsForSelectedDate count: ${screeningsForSelectedDate.length}`);


    if (loadingSchedules && !schedulesFromApi) {
        return <p className={cx('loading-text', 'text-center my-3')}>Đang tải lịch chiếu...</p>;
    }

    if (errorSchedules) {
        return <p className={cx('error-text', 'text-danger', 'text-center my-3')}>{errorSchedules}</p>;
    }
    
    if (!movie || !movie.id) { // Check movie prop before trying to render showtimes
        return <p className={cx('no-schedules-text', 'text-center my-3')}>Vui lòng chọn phim để xem lịch chiếu.</p>;
    }

    if (!loadingSchedules && (!schedulesFromApi || Object.keys(schedulesFromApi).length === 0)) {
        return <p className={cx('no-schedules-text', 'text-center my-3')}>Hiện chưa có lịch chiếu cho phim này.</p>;
    }

    return (
        <div className={cx('schedule-wrapper', 'border-1', 'border-gray', 'rounded-3', 'w-100', 'p-3')}>
            <div className={cx('date-selector-strip', 'd-flex', 'justify-content-start', 'gap-2', 'border-bottom', 'border-secondary', 'pb-3', 'mb-3')}>
                {displayDays.map((dayDateObj, idx) => {
                    const isToday = isSameDay(dayDateObj, startOfDay(new Date()));
                    const isCurrentlySelected = selectedDate ? isSameDay(dayDateObj, selectedDate) : false;
                    const dayDateStr = format(dayDateObj, 'yyyy-MM-dd');
                    const hasScheduleForThisDisplayDay = schedulesFromApi && schedulesFromApi[dayDateStr] && schedulesFromApi[dayDateStr].length > 0;

                    return (
                        <button
                            key={idx}
                            className={cx(
                                'date-button',
                                'btn', 'd-flex', 'flex-column', 'align-items-center', 'justify-content-center', 'p-0',
                                'border', 'rounded', 'over-hidden',
                                { 'active-date': isCurrentlySelected, 'btn-light-custom': !isCurrentlySelected, 'disabled-date': !hasScheduleForThisDisplayDay }
                            )}
                            style={{ width: '60px', height: '58px' }}
                            onClick={() => hasScheduleForThisDisplayDay && handleDateSelect(dayDateObj)}
                            disabled={!hasScheduleForThisDisplayDay}
                        >
                            <strong
                                className={cx('date-button-day', 'w-100', 'h-50', 'd-flex', 'align-items-center', 'justify-content-center')}
                                style={{ fontSize: '18px' }}
                            >
                                {format(dayDateObj, 'd')}
                            </strong>
                            <p className={cx('date-button-weekday', 'fs-8', 'h-50', 'd-flex', 'justify-content-center', 'align-items-center', 'm-0')}>
                                {isToday ? 'Hôm nay' : format(dayDateObj, 'eee', { locale: vi })}
                            </p>
                        </button>
                    );
                })}
            </div>

           {/* Showtime display section */}
            {loadingSchedules && schedulesFromApi && <p className={cx('loading-text', 'text-center my-3')}>Đang cập nhật suất chiếu...</p>}

            {!loadingSchedules && selectedDateString && (
                 screeningsForSelectedDate.length > 0 ? (
                    <div className={cx('time-slots-container', 'd-flex', 'flex-wrap', 'gap-2', 'mt-3', 'w-100')}>
                        {screeningsForSelectedDate.map((screeningTime) => ( // screeningTime is an object from API
                            <button
                                key={screeningTime.screeningId}
                                onClick={() => handleTimeSelect(screeningTime)} 
                                className={cx('time-slot-button', 'border-1', 'p-1', 'pe-2', 'ps-2', 'rounded-2', 'fs-7', 'red-hover', 'me-1')}
                                type="button"
                            >
                                <span className={cx('fw-bold', 'fs-6')}>{formatTimeForDisplay(screeningTime.time)}</span>
                                {movie?.duration ? ` - ${getEndTime(screeningTime.time, movie.duration)}` : ''}
                            </button>
                        ))}
                    </div>
                ) : (
                    <p className={cx('no-schedules-for-date', 'text-center my-3')}>
                        Không có suất chiếu cho ngày {selectedDate ? format(selectedDate, 'dd/MM/yyyy', { locale: vi }) : ''}.
                    </p>
                )
            )}
            {!loadingSchedules && !selectedDateString && Object.keys(schedulesFromApi || {}).length > 0 && (
                <p className={cx('no-schedules-for-date', 'text-center my-3')}>Vui lòng chọn ngày để xem suất chiếu.</p>
            )}
        </div>
    );
};

export default BookingSchedule;