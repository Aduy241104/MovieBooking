import classNames from "classnames/bind"
import styles from './PlayingMovie.module.scss'
import SwiperSlides from "../SwiperSlide/SwiperSlides";
import { SwiperSlide } from 'swiper/react';
import MovieComp from "../MovieComp";
import { useEffect, useState } from "react";
import { getNowShowingMovieAPI, getUpComingMovieAPI } from "../../service/TheMovieService";


const cx = classNames.bind(styles);


function PlayingMovie() {
    const [listMovie, setListMovie] = useState([]);



    const fetchData = async () => {
        try {
            const response = await getNowShowingMovieAPI();
            setListMovie(response);
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
                    <h2 className={ cx('pb-5', 'bg-text')}>Phim sắp chiếu</h2>
                    <div className={ cx('w-responsive', 'pb-5') }>
                        <SwiperSlides>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ 'https://api-website.cinestar.com.vn/media/wysiwyg/NEWS/mission-impoossible-jpg-7332-1731381379.png' } />
                            </SwiperSlide>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ "https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/v/i/virus-main_poster-2.jpg" } />
                            </SwiperSlide>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ "https://metiz.vn/media/poster_film/ba-mat.jpg" } />
                            </SwiperSlide>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTITafLS1S1kDGOG8OvmjmYdhYsfPI69TZ9PQ&s' } />
                            </SwiperSlide>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/z/o/zootopia_2_-_teaser_poster_up.jpg' } />
                            </SwiperSlide>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ "https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/v/i/virus-main_poster-2.jpg" } />
                            </SwiperSlide>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ "https://metiz.vn/media/poster_film/ba-mat.jpg" } />
                            </SwiperSlide>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTITafLS1S1kDGOG8OvmjmYdhYsfPI69TZ9PQ&s' } />
                            </SwiperSlide>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/z/o/zootopia_2_-_teaser_poster_up.jpg' } />
                            </SwiperSlide>
                        </SwiperSlides>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default PlayingMovie