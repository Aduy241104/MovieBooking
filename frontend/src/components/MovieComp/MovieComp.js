import classNames from "classnames/bind"
import styles from './Movie.module.scss'

const cx = classNames.bind(styles);

function MovieComp({ imglink }) {
    return (
        <div className={ cx('wrapper', 'w-responsive')}>
            <div className={cx('poster')}>
                <img src={ imglink } alt="" />
            </div>
            <div className="text-light">
                <p className="line-clamp-1">Nhiệm vụ Bất khả thi thi thi</p>

            </div>
        </div>
    )
}

export default MovieComp