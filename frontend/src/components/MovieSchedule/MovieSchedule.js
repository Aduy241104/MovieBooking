import React, { useEffect, useState } from 'react';
import { Button } from 'antd';
import { WarningOutlined } from '@ant-design/icons';
import moment from 'moment';
import 'moment/locale/vi';
import MovieItem from './MovieItem';
import { getMovieByDateAPI } from '../../service/TheMovieService';

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
                const response = await getMovieByDateAPI();
                setMovieList(response.result);
            } catch (error) {
                setError("Khoont thể tải dữ liệu")
            }
        }
        fetchData();

    }, [selectedDate])

    const days = getNext7Days();

    return (
        <div className='container d-flex flex-column align-items-center text-light mt-5 pb-5 pt-3'>
            <h2 className='p-5 pt-1 text-center bg-text-2'>Lịch chiếu phim</h2>
            <div className='border-1 border-gray rounded-3 w-75 p-3'>
                <div className='d-flex justify-content-start gap-2 border-bottom border-gray pb-3'>
                    { days.map((day, idx) => {
                        const isToday = day.isSame(moment(), 'day');
                        const isSelected = selectedDate === day.format('YYYY-MM-DD');

                        return (
                            <button
                                key={ idx }
                                className={ `btn d-flex flex-column align-items-center justify-content-center p-0 
                ${isSelected ? 'bg-red btn-danger text-black' : 'btn-light'} 
                border rounded over-hidden`}
                                style={ { width: '60px', height: '60px' } }
                                onClick={ () => setSelectedDate(day.format('YYYY-MM-DD')) }
                            >
                                <strong className='w-100 h-50 bg-light d-flex align-items-center justify-content-center' style={ { fontSize: '22px' } }>{ day.date() }</strong>
                                <small className='h-50 d-flex justify-content-center align-items-center'>{ isToday ? 'Hôm nay' : day.format('dddd') }</small>
                            </button>
                        );
                    }) }
                </div>

                <div className='w-100 mt-4 custome-scroll-bar custome-scroll-bar-light' style={ { maxHeight: '400px', overflowY: 'auto' } }>
                    { error ? (
                        <div className="w-100 text-center text-danger mt-4">
                            <WarningOutlined style={ { fontSize: 40, color: 'red' } } />
                            <p className="mt-2 pb-3">{ error }</p>
                            <Button className='bg-red text-black' >Thử lại</Button>
                        </div>
                    ) : (
                        (movieList.length === 0) ? (
                            <div className='w-100 text-center p-5'>
                                <i className="fa-solid fa-video fs-4"></i>
                                <p className="text-secondary">Hiện chưa có phim nào để hiển thị.</p>
                            </div>

                        ) : (
                            movieList.map((item, index) => {
                                return (
                                    <MovieItem key={ index } data={ item } />
                                )
                            })
                        )
                    ) }
                </div>


                {/* <div className='text-center mt-4'>
                    <h5>Ngày đã chọn: { selectedDate }</h5>
                </div> */}
            </div>
        </div>
    );
}

export default MovieSchedule;
