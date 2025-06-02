import classNames from "classnames/bind"
import styles from './ComingSoon.module.scss'
import SwiperSlides from "../SwiperSlide/SwiperSlides";
import { SwiperSlide } from 'swiper/react';
import MovieComp from "../MovieComp";
import CustomizeText from "../CustomizeText";


const cx = classNames.bind(styles);

function ComingSoon() {
    return (
        <div className="p-5" >
            <div className="container">
                <div className="d-flex flex-column justify-content-center align-items-center text-light">
                    <h2 className="pb-5">Phim đang chiếu</h2>
                    <div className={ cx('w-responsive') }>
                        <SwiperSlides>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ 'https://api-website.cinestar.com.vn/media/wysiwyg/NEWS/mission-impoossible-jpg-7332-1731381379.png' } />
                            </SwiperSlide>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ "https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/t/e/teaser_poster_bringherback_a24_sony_1_.jpg" } />
                            </SwiperSlide>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ "https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/t/e/teaser_poster_bringherback_a24_sony_1_.jpg" } />
                            </SwiperSlide>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ 'https://api-website.cinestar.com.vn/media/wysiwyg/NEWS/mission-impoossible-jpg-7332-1731381379.png' } />
                            </SwiperSlide>
                            <SwiperSlide>
                                <MovieComp
                                    imglink={ 'https://api-website.cinestar.com.vn/media/wysiwyg/NEWS/mission-impoossible-jpg-7332-1731381379.png' } />
                            </SwiperSlide>
                            <SwiperSlide>Slide 4</SwiperSlide>
                            <SwiperSlide>Slide 3</SwiperSlide>
                            <SwiperSlide>Slide 4</SwiperSlide>
                        </SwiperSlides>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default ComingSoon