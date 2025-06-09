import classNames from "classnames/bind"
import styles from './Movie.module.scss'

const cx = classNames.bind(styles);

function MovieComp({ imglink }) {
    return (
        <div className={ cx('wrapper', 'w-responsive') }>
            <div className={ cx('poster', 'border border-1 border-dark rounded-1') }>
                <img src={ imglink } alt="" />
            </div>
            <div className="text-light">
                <p className="line-clamp-1 fw-medium">Nhiệm vụ Bất khả thi thi thi</p>
                <p className={ cx('genre', 'text-secondary') }>Hành động, trinh thám</p>
                <p className={ cx('rating') }><i className="fa-solid fa-star text-warning"></i> 9.5</p>
            </div>
        </div>
    )
}

export default MovieComp