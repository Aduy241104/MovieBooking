import { memo, useEffect, useState } from 'react';
import { Button } from 'antd';
import { WarningOutlined } from '@ant-design/icons';
import moment from 'moment';
import 'moment/locale/vi';
import MovieItem from './MovieItem';
import { getMovieByDateAPI } from '../../service/TheMovieService';
import classNames from 'classnames/bind';
import styles from './MovieSchedule.module.scss'

const cx = classNames.bind(styles);

moment.locale('vi');

function MovieSchedule() {
    const [selectedDate, setSelectedDate] = useState(moment().format('YYYY-MM-DD'));
    const [movieList, setMovieList] = useState([]);
    const [error, setError] = useState(null);

    const getNext7Days = () => {
        return Array.from({ length: 7 }, (_, i) => moment().add(i, 'days'));
    };

    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await getMovieByDateAPI(selectedDate);
                setMovieList(response.result);
            } catch (error) {
                setError("Khong thể tải dữ liệu")
            }
        }
        fetchData();

    }, [selectedDate])

    const days = getNext7Days();

    return (
        <div className='container d-flex flex-column align-items-center text-light mt-5 pb-5 pt-3'>
            <h2 className={ cx('textRed', 'text-center', 'px-3', 'pt-1 pb-5', 'bg-text-2') }>Lịch chiếu phim</h2>

            <div className={ cx('wrapper', 'border', 'border-secondary', 'rounded-3', 'p-3') }>
                {/* Danh sách ngày */ }
                <div className={ cx('dateScroll') }>
                    <div className={ cx('scrollContent') }>
                        { days.map((day, idx) => {
                            const isToday = day.isSame(moment(), 'day');
                            const isSelected = selectedDate === day.format('YYYY-MM-DD');

                            return (
                                <button
                                    key={ idx }
                                    className={ cx('dateButton', 'btn', 'd-flex', 'flex-column', 'align-items-center', 'justify-content-center', 'p-0', 'border', 'rounded', 'overflow-hidden', {
                                        'bg-red': isSelected,
                                        'text-black': isSelected,
                                        'btn-light': !isSelected,
                                    }) }
                                    onClick={ () => setSelectedDate(day.format('YYYY-MM-DD')) }
                                >
                                    <strong className='w-100 h-50 bg-light d-flex align-items-center justify-content-center'>
                                        { day.date() }
                                    </strong>
                                    <small className='h-50 d-flex justify-content-center align-items-center'>
                                        { isToday ? 'Hôm nay' : day.format('ddd') }
                                    </small>
                                </button>
                            );
                        }) }
                    </div>
                </div>

                {/* Danh sách phim */ }
                <div className='w-100 mt-4 custome-scroll-bar custome-scroll-bar-light' style={ { maxHeight: '400px', overflowY: 'auto' } }>
                    { error ? (
                        <div className="w-100 text-center text-danger mt-4">
                            <WarningOutlined style={ { fontSize: 40, color: 'red' } } />
                            <p className="mt-2 pb-3">{ error }</p>
                            <Button className='bg-red text-black'>Thử lại</Button>
                        </div>
                    ) : (
                        (movieList.length === 0) ? (
                            <div className='w-100 text-center p-5'>
                                <i className="fa-solid fa-video fs-4"></i>
                                <p className="text-secondary">Hiện chưa có phim nào để hiển thị.</p>
                            </div>
                        ) : (
                            movieList.map((item, index) => (
                                <MovieItem key={ index } data={ item } />
                            ))
                        )
                    ) }
                </div>
            </div>
        </div>
    

    );
}

export default memo(MovieSchedule);
