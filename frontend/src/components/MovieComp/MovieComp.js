import classNames from "classnames/bind"
import styles from './Movie.module.scss'
import { useNavigate } from "react-router-dom";

const cx = classNames.bind(styles);

function MovieComp({ imglink, index }) {
    const navigate = useNavigate();

    return (
        <div className={ cx('wrapper', 'w-responsive') } onClick={ () => navigate('/movie-detail/1') }>
            <div className={ cx('poster', 'rounded-3') }>
                <img src={ imglink } alt="" />
            </div>
            <div className="text-light d-flex">
                <div className={ cx("number-rank") }>{ index }</div>
                <div>
                    <p className="line-clamp-1 fw-medium fs-6">Nhiệm vụ Bất khả thi thi thi</p>
                    <p className={ cx('genre', 'text-secondary fs-7') }>Hành động, trinh thám</p>
                </div>
            </div>
        </div>
    )
}

export default MovieComp