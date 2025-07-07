import classNames from 'classnames/bind'
import styles from './Rotate.module.scss'

const cx = classNames.bind(styles);

function CardRotate() {
    return (
        <div className='d-flex justify-content-center w-100'>
            <div className='d-flex justify-content-start mt-4' style={ { overflowX: 'scroll', width: '1400px' } }>

                <div className={ cx('wrapper-card') }>
                    <img
                        src="https://static.nutscdn.com/vimg/400-0/ad92b01db521be321b3b79ee94869555.jpg"
                        alt=""
                        className={ cx('card-image') }
                    />
                </div>

                {/* <div className={ cx('wrapper-card', 'wrapper-card-left') }>
                    <img src="https://static.nutscdn.com/vimg/400-0/ad92b01db521be321b3b79ee94869555.jpg" alt="" />
                </div>
                <div className={ cx('wrapper-card', 'wrapper-card-right') }>
                    <img src="https://static.nutscdn.com/vimg/400-0/ad92b01db521be321b3b79ee94869555.jpg" alt="" />
                </div>
                <div className={ cx('wrapper-card', 'wrapper-card-left') }>
                    <img src="https://static.nutscdn.com/vimg/400-0/ad92b01db521be321b3b79ee94869555.jpg" alt="" />
                </div>
                <div className={ cx('wrapper-card', 'wrapper-card-right') }>
                    <img src="https://static.nutscdn.com/vimg/400-0/ad92b01db521be321b3b79ee94869555.jpg" alt="" />
                </div>
                <div className={ cx('wrapper-card', 'wrapper-card-left') }>
                    <img src="https://static.nutscdn.com/vimg/400-0/ad92b01db521be321b3b79ee94869555.jpg" alt="" />
                </div>
                <div className={ cx('wrapper-card', 'wrapper-card-right') }>
                    <img src="https://static.nutscdn.com/vimg/400-0/ad92b01db521be321b3b79ee94869555.jpg" alt="" />
                </div>
                <div className={ cx('wrapper-card', 'wrapper-card-left') }>
                    <img src="https://static.nutscdn.com/vimg/400-0/ad92b01db521be321b3b79ee94869555.jpg" alt="" />
                </div>
                <div className={ cx('wrapper-card', 'wrapper-card-right') }>
                    <img src="https://static.nutscdn.com/vimg/400-0/ad92b01db521be321b3b79ee94869555.jpg" alt="" />
                </div>
                <div className={ cx('wrapper-card', 'wrapper-card-left') }>
                    <img src="https://static.nutscdn.com/vimg/400-0/ad92b01db521be321b3b79ee94869555.jpg" alt="" />
                </div>
                <div className={ cx('wrapper-card', 'wrapper-card-right') }>
                    <img src="https://static.nutscdn.com/vimg/400-0/ad92b01db521be321b3b79ee94869555.jpg" alt="" />
                </div> */}
            </div>
        </div>
    )
}

export default CardRotate