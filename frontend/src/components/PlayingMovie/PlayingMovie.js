import classNames from "classnames/bind"
import styles from './PlayingMovie.module.scss'
import SwiperSlides from "../SwiperSlide/SwiperSlides";
import { SwiperSlide } from 'swiper/react';
import MovieComp from "../MovieComp";
import { useEffect, useState } from "react";
// import { getNowShowingMovieAPI } from "../../service/TheMovieService";
import { memo } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchNowPlaying } from '../../redux/slices/movieSlice'

const cx = classNames.bind(styles);

function PlayingMovie() {
    // const [listMovie, setListMovie] = useState([]);
    const dispatch = useDispatch();
    const nowPlaying = useSelector(state => state.movie.nowPlaying);


    useEffect(() => {
        if (nowPlaying.status === "idle") {
            dispatch(fetchNowPlaying())
        }
    }, [dispatch, nowPlaying.status])


    console.log("test data from redux: ", nowPlaying);


    // useEffect(() => {
    //     const fetchData = async () => {
    //         try {
    //             const response = await getNowShowingMovieAPI();
    //             setListMovie(response.result);
    //         } catch (error) {
    //             console.log(error.message);
    //         }
    //     }
    //     fetchData();
    // }, []);

    return (
        <div className="pt-5 mt-3">
            <div className={ cx("container") }>
                <div className="d-flex flex-column justify-content-center align-items-center text-light mt-5">
                    <h2 className={ cx('pb-5', 'bg-text') }>Phim đang chiếu</h2>
                    <div className={ cx('w-responsive', 'pb-5') }>
                        <SwiperSlides>
                            { nowPlaying.movies.map((item, index) => {
                                return (
                                    <SwiperSlide key={ index }>
                                        <MovieComp
                                            index={ index + 1 }
                                            imglink={ item.smallImage }
                                            nameVN={ item.nameVN }
                                            types={ item.types }
                                            ageLimit={ item.ageLimit }
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

export default memo(PlayingMovie)