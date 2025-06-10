import CustomizeButton from '../../../components/CustomeButton';
import Schedule from '../Schedule/Schedule';
import styles from './mss.module.scss'
import classNames from 'classnames/bind'

const cx = classNames.bind(styles);

function RightComponent() {
    return (
        <div>
            <div className='w-100 d-flex justify-content-between'>
                <CustomizeButton className={ cx('fw-bold') } gold rounded large leftIcon={ <i className="fa-regular fa-circle-play fs-5"></i> }>
                    Xem Trailer
                </CustomizeButton>

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