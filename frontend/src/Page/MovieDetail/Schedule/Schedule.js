import { useState } from 'react';
import moment from 'moment';
import 'moment/locale/vi';

function Schedule() {
    const [selectedDate, setSelectedDate] = useState(moment().format('YYYY-MM-DD'));

    const getNext7Days = () => {
        return Array.from({ length: 8 }, (_, i) => moment().add(i, 'days'));
    };

    const days = getNext7Days();
    return (

        <div className='border-1 border-gray rounded-3 w-75 p-3'>
            <div className='d-flex justify-content-start gap-2 border-bottom border-secondary pb-3'>
                { days.map((day, idx) => {
                    const isToday = day.isSame(moment(), 'day');
                    const isSelected = selectedDate === day.format('YYYY-MM-DD');

                    return (
                        <button
                            key={ idx }
                            className={ `btn d-flex flex-column align-items-center justify-content-center p-0 
        ${isSelected ? 'gardient-sunshine fw-bold' : 'btn-light'} 
        border rounded over-hidden`}
                            style={ { width: '60px', height: '58px' } }
                            onClick={ () => setSelectedDate(day.format('YYYY-MM-DD')) }
                        >
                            <strong className='w-100 text-black h-50 bg-light d-flex align-items-center justify-content-center' style={ { fontSize: '18px' } }>{ day.date() }</strong>
                            <p className='fs-8 h-50 d-flex justify-content-center align-items-center'>{ isToday ? 'Hôm nay' : day.format('dddd') }</p>
                        </button>
                    );
                }) }
            </div>

            <div className='d-flex flex-wrap gap-2 mt-3 w-100'>
                <button className='border-1 p-1 pe-2 ps-2 rounded-2 fs-7 red-hover me-1'>
                    <span className='fw-bold fs-6'>14:50</span> - 16:35
                </button>

                <button className='border-1 p-1 pe-2 ps-2 rounded-2 fs-7 red-hover me-1'>
                    <span>14:50</span> - 16:35
                </button>
                <button className='border-1 p-1 pe-2 ps-2 rounded-2 fs-7 red-hover me-1'>
                    <span>14:50</span> - 16:35
                </button>
            </div>
        </div>


    )
}

export default Schedule