import React, { useState } from 'react';
import moment from 'moment';
import 'moment/locale/vi';
import MovieItem from './MovieItem';

moment.locale('vi');

function MovieSchedule() {
    const [selectedDate, setSelectedDate] = useState(moment().format('YYYY-MM-DD'));

    const getNext7Days = () => {
        return Array.from({ length: 7 }, (_, i) => moment().add(i, 'days'));
    };

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
                    <MovieItem />
                    <MovieItem />
                    <MovieItem />
                </div>


                {/* <div className='text-center mt-4'>
                    <h5>Ngày đã chọn: { moment(selectedDate).format('dddd, DD/MM/YYYY') }</h5>
                </div> */}
            </div>
        </div>
    );
}

export default MovieSchedule;
