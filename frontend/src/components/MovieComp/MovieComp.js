import classNames from "classnames/bind"
import styles from './Movie.module.scss'
import { useNavigate } from "react-router-dom";

const cx = classNames.bind(styles);

function MovieComp({ imglink, types = [], nameVN, index, id, ageLimit = 0 }) {
    const navigate = useNavigate();

    const checkAgeLimit = (age) => {
        if (age >= 18) {
            return "tag_red";
        } else if (age >= 16) {
            return "tag_orange";
        }
    }

    return (
        <div className={ cx('wrapper', 'w-responsive') } onClick={ () => navigate(`/movie-detail/${id}`) }>
            <div className={ cx('poster', 'rounded-3') }>
                <img
                    src={ imglink }
                    alt=""
                    loading="lazy"
                    onError={ (e) => {
                        e.target.onerror = null; // Ngăn lặp vô hạn nếu ảnh fallback cũng lỗi
                        e.target.src = "https://upload.wikimedia.org/wikipedia/commons/1/14/No_Image_Available.jpg"; // Đường dẫn ảnh mặc định
                    } }
                />

                <div className={ cx('age-limit-tag', `${checkAgeLimit(ageLimit)}`) }>
                    { ageLimit < 16 ? "k" : ageLimit + "+" }
                </div>
            </div>
            <div className="text-light d-flex mt-1">
                <div className={ cx("number-rank") }>{ index }</div>
                <div>
                    <p className={ cx('line-clamp-1 fw-medium fs-6', 'nameVN')}>{ nameVN }</p>
                    <p className={ cx('genre', 'text-secondary fs-7 line-clamp-1') }>{ types.join(', ') }</p>
                </div>
            </div>
        </div>
    )
}

export default MovieComp