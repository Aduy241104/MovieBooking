import { useState } from 'react';
import CustomizeButton from '../../../components/CustomeButton';
//import Schedule from '../Schedule/Schedule';
import TrailerModal from '../TrailerModal/TrailerModal';
import styles from './mss.module.scss'
import classNames from 'classnames/bind'
import BookingSchedule from '../../Booking/BookingSchedule/BookingSchedule'; //locpng

const cx = classNames.bind(styles);

function RightComponent(props) {
    const [isOpenTrailer, setOpenTrailer] = useState(false);
    const { movie} = props; // <<<< NHẬN movie TỪ PROPS/locpng
   
    return (
        <div>
            <div className='w-100 d-flex justify-content-between'>
                <CustomizeButton
                    className={cx('fw-bold shadow-hover-gold')}
                    gold rounded large leftIcon={<i className="fa-regular fa-circle-play fs-5"></i>}
                    onClick={() => setOpenTrailer(true)}
                >
                    Xem Trailer
                </CustomizeButton>

                <TrailerModal
                    open={isOpenTrailer}
                    onClose={() => setOpenTrailer(false)}
                    // trailerUrl={props.trailer}
                    trailerUrl={movie?.trailer} // Sửa lại để xem movie
                />

                <CustomizeButton
                    className={ cx('fw-bold text-light', 'blue')}
                    roundedBig
                    large
                    leftIcon={ <i className="fa-solid fa-comment-dots"></i> }>
                    Xem đánh giá
                </CustomizeButton>
            </div>
            <div className='mt-3'>
                <p>Ngày chiếu: <span className={cx('text-gray')}>{props.startDate}</span></p>

                <div className='mt-3'>
                    <h5 className='pb-2'>Lịch chiếu </h5>
                    {/*  <Schedule /> */}

                       {/* Truyền movie prop vào BookingSchedule/ locpng */}
                    {movie && movie.id ? (
                        <BookingSchedule movie={movie} /> 
                    ) : (
                        <p>Đang tải thông tin phim...</p> 
                    )}

                </div>
            </div>
        </div>
    )
}

export default RightComponent