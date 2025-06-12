import classNames from "classnames/bind"
import styles from './PlayingMovie.module.scss'
import SwiperSlides from "../SwiperSlide/SwiperSlides";
import { SwiperSlide } from 'swiper/react';
import MovieComp from "../MovieComp";
import { useEffect, useState } from "react";
import { getNowShowingMovieAPI } from "../../service/TheMovieService";


const cx = classNames.bind(styles);


function PlayingMovie() {
    const [listMovie, setListMovie] = useState([]);

    const fetchData = async () => {
        try {
            const response = await getNowShowingMovieAPI();
            console.log("test: ", response.result);
            setListMovie(response.result);
        } catch (error) {
            console.log(error.message);
        }
    }


    useEffect(() => {
        fetchData();
    }, [])
    return (
        <div
            className="p-5"

        >
            <div className={ cx("container") }>
                <div className="d-flex flex-column justify-content-center align-items-center text-light">
                    <h2 className={ cx('pb-5', 'bg-text') }>Phim đang chiếu</h2>
                    <div className={ cx('w-responsive', 'pb-5') }>
                        <SwiperSlides>
                            { listMovie.map((item, index) => {
                                return (
                                    <SwiperSlide key={ index }>
                                        <MovieComp
                                            index={ index + 1 }
                                            imglink={ item.smallImage }
                                            nameVN={ item.nameVN }
                                            types={ item.types }
                                            id={ item.id }
                                        />
                                    </SwiperSlide>
                                )
                            }) }
                        </SwiperSlides>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default PlayingMovie