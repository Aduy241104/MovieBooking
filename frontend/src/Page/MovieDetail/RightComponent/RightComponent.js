import { useState } from 'react';
import CustomizeButton from '../../../components/CustomeButton';
import Schedule from '../Schedule/Schedule';
import TrailerModal from '../TrailerModal/TrailerModal';
import styles from './mss.module.scss'
import classNames from 'classnames/bind'

const cx = classNames.bind(styles);

function RightComponent() {
    const [isOpenTrailer, setOpenTrailer] = useState(false);


    return (
        <div>
            <div className='w-100 d-flex justify-content-between'>
                <CustomizeButton
                    className={ cx('fw-bold shadow-hover-gold') }
                    gold rounded large leftIcon={ <i className="fa-regular fa-circle-play fs-5"></i> }
                    onClick={ () => setOpenTrailer(true) }
                >
                    Xem Trailer
                </CustomizeButton>

                <TrailerModal
                    open={ isOpenTrailer }
                    onClose={ () => setOpenTrailer(false) }
                    trailerUrl={ 'https://www.youtube.com/embed/AhvXh0Rv0_c?si=sgrOwTQ2xcAnVso8' }
                />

                <CustomizeButton
                    className={ cx('fw-bold gardient-midNight text-light') }
                    gold
                    roundedBig
                    large
                    leftIcon={ <i className="fa-solid fa-ice-cream"></i> }>
                    Xem đánh giá
                </CustomizeButton>
            </div>
            <div className='mt-3'>
                <p><strong>Ngày chiếu:</strong> <span className={ cx('text-gray') }>13/06/2025</span></p>

                <div className='mt-3'>
                    <h5 className='pb-2 fw-bolder'>Lịch chiếu </h5>

                    <Schedule />

                </div>
            </div>
        </div>
    )
}

export default RightComponent