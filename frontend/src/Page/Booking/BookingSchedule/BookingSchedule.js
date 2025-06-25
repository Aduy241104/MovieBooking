import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMovieSchedules } from '../../../service/ScreeningService'; // TODO: KIỂM TRA LẠI ĐƯỜNG DẪN NÀY
import { format, parseISO, addDays, isSameDay, startOfDay } from 'date-fns';
import { vi } from 'date-fns/locale';
import { AuthContext } from '../../../context/AuthContext'; // TODO: KIỂM TRA LẠI ĐƯỜNG DẪN NÀY
import styles from './bookingSchedule.module.scss';
import classNames from 'classnames/bind';

const cx = classNames.bind(styles);

const BookingSchedule = ({ movie }) => {
    // schedulesFromApi: Dữ liệu gốc từ API { "YYYY-MM-DD": [suất chiếu], ... }
    const [schedulesFromApi, setSchedulesFromApi] = useState(null);
    const [loadingSchedules, setLoadingSchedules] = useState(false);
    const [errorSchedules, setErrorSchedules] = useState('');

    // selectedDate: Date object, không phải string, để dễ so sánh và format
    const [selectedDate, setSelectedDate] = useState(startOfDay(new Date())); // Mặc định chọn hôm nay

    const navigate = useNavigate();
    const { user } = useContext(AuthContext);

    // Tạo danh sách 8 ngày để hiển thị, bắt đầu từ hôm nay
    const displayDays = Array.from({ length: 8 }, (_, i) => addDays(startOfDay(new Date()), i));

    useEffect(() => {
        console.log('BookingSchedule.js - useEffect - movie prop:', movie);
        if (movie && movie.id) {
            const fetchSchedules = async () => {
                setLoadingSchedules(true);
                setErrorSchedules('');
                setSchedulesFromApi(null); // Reset trước khi fetch
                // Không reset selectedDate ở đây, để giữ lại lựa chọn của user nếu movie không đổi
                // Hoặc reset nếu logic yêu cầu: setSelectedDate(startOfDay(new Date()));

                try {
                    const response = await getMovieSchedules(movie.id);
                    console.log('BookingSchedule.js - API getMovieSchedules FULL response.data:', response.data);

                    if (response.data && response.data.result && response.data.result.schedules) {
                        const fetchedApiSchedules = response.data.result.schedules;
                        console.log('BookingSchedule.js - Value of response.data.result.schedules (fetchedApiSchedules):', fetchedApiSchedules);
                        setSchedulesFromApi(fetchedApiSchedules);

                        // Kiểm tra xem selectedDate hiện tại có lịch chiếu không, nếu không và có lịch thì chọn ngày đầu tiên có lịch
                        // Hoặc, nếu muốn luôn ưu tiên selectedDate hiện tại (nếu nó nằm trong displayDays), thì không cần thay đổi
                        const currentSelectedDateStr = format(selectedDate, 'yyyy-MM-dd');
                        if (!fetchedApiSchedules[currentSelectedDateStr]) {
                            const apiDatesWithSchedules = Object.keys(fetchedApiSchedules).filter(dateStr => fetchedApiSchedules[dateStr]?.length > 0).sort();
                            if (apiDatesWithSchedules.length > 0) {
                                // Nếu muốn tự động nhảy đến ngày đầu tiên CÓ lịch chiếu thực sự
                                // setSelectedDate(startOfDay(parseISO(apiDatesWithSchedules[0])));
                                // console.log('BookingSchedule.js - Auto-selected first available date from API:', apiDatesWithSchedules[0]);
                            }
                        }

                    } else {
                        console.log('BookingSchedule.js - API response does not have expected structure or schedules is missing. Response result:', response.data?.result);
                        setSchedulesFromApi({}); // Set là object rỗng nếu không có schedules
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
            setSchedulesFromApi(null); // Reset nếu không có movie hoặc movie.id
            // setSelectedDate(startOfDay(new Date())); // Có thể reset selectedDate về hôm nay
             if (movie) {
                console.log('BookingSchedule.js - useEffect: Movie prop is present but missing id.');
            } else {
                console.log('BookingSchedule.js - useEffect: Movie prop is null or undefined.');
            }
        }
    }, [movie]); // Chỉ phụ thuộc vào movie

    const handleDateSelect = (dateObject) => {
        console.log('BookingSchedule.js - Date selected by user (object):', dateObject);
        setSelectedDate(dateObject);
    };

    const handleTimeSelect = (screeningInfo) => { // movie.duration đã được truyền vào `getEndTime`
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
            showDateTime: showDateTimeObject.toISOString(), // Giữ lại showDateTime dạng ISO string cho trang booking
        };
        console.log('BookingSchedule.js - Navigating to booking with screeningInfo:', fullScreeningInfo, 'and movieInfo:', movie);
        navigate('/booking', { state: { screeningInfo: fullScreeningInfo, movieInfo: movie } });
    };

    const formatDateForDisplay = (dateObject) => { // Nhận Date object
        try {
            return format(dateObject, 'eee, dd/MM', { locale: vi }); // Thay đổi format nếu muốn
        } catch (e) {
            console.warn(`Error formatting date object: ${dateObject}`, e);
            return 'N/A';
        }
    };
    
    // Hàm này được định nghĩa ở đây
    const formatTimeForDisplay = (timeStr) => { // API trả về time là string "HH:mm:ss"
        if (typeof timeStr === 'string') {
            const parts = timeStr.split(':');
            if (parts.length >= 2) {
                return `${parts[0]}:${parts[1]}`; // Lấy HH:mm
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
            // Cần một ngày cụ thể để tính toán, có thể dùng selectedDate
            const startDate = new Date(selectedDate); // Hoặc new Date() nếu chỉ cần tính khoảng thời gian
            startDate.setHours(hours, minutes, 0, 0);

            const endDate = new Date(startDate.getTime() + durationMinutes * 60000); // Thêm duration (ms)
            return format(endDate, 'HH:mm');
        } catch (e) {
            console.error('Error calculating end time:', e);
            return '';
        }
    };

    // Lấy các suất chiếu cho ngày đang được chọn (selectedDate)
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
    
    if (!movie || !movie.id) { // Kiểm tra movie prop trước khi cố gắng render lịch chiếu
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

            {/* Phần hiển thị suất chiếu */}
            {loadingSchedules && schedulesFromApi && <p className={cx('loading-text', 'text-center my-3')}>Đang cập nhật suất chiếu...</p>}

            {!loadingSchedules && selectedDateString && (
                 screeningsForSelectedDate.length > 0 ? (
                    <div className={cx('time-slots-container', 'd-flex', 'flex-wrap', 'gap-2', 'mt-3', 'w-100')}>
                        {screeningsForSelectedDate.map((screeningTime) => ( // screeningTime là object từ API
                            <button
                                key={screeningTime.screeningId}
                                onClick={() => handleTimeSelect(screeningTime)} // Truyền nguyên object screeningTime
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