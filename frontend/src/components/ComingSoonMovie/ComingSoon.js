import classNames from "classnames/bind"
import styles from './ComingSoon.module.scss'
import SwiperSlides from "../SwiperSlide/SwiperSlides";
import { SwiperSlide } from 'swiper/react';
import MovieComp from "../MovieComp";
import { memo, useEffect, useState } from "react";
// import { getUpComingMovieAPI } from "../../service/TheMovieService";
import { useDispatch, useSelector } from "react-redux";
import { fetchComingSoon } from "../../redux/slices/movieSlice";

const cx = classNames.bind(styles);

function ComingSoon() {
    // const [listMovie, setListMovie] = useState([]);
    const dispatch = useDispatch();
    const comingSoonMovie = useSelector(state => state.movie.comingSoon);


    useEffect(() => {
        if (comingSoonMovie.status === "idle") {
            dispatch(fetchComingSoon());
        }

    }, [dispatch, comingSoonMovie.status])


    console.log("coming soon section: ", comingSoonMovie);

    // const fetchData = async () => {
    //     try {
    //         const response = await getUpComingMovieAPI();
    //         setListMovie(response.result);
    //     } catch (error) {
    //         console.log(error.message);
    //     }
    // }

    // useEffect(() => {
    //     fetchData()
    // }, [])
    return (
        <div className="pt-5" >
            <div className="container">
                <div className="d-flex flex-column justify-content-center align-items-center text-light">
                    <h2 className={ cx('pb-5', 'bg-text') }>Phim sắp chiếu</h2>
                    <div className={ cx('w-responsive') }>
                        <SwiperSlides>
                            { comingSoonMovie.movies.map((item, index) => {
                                return (
                                    <SwiperSlide key={ index }>
                                        <MovieComp
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

export default memo(ComingSoon)