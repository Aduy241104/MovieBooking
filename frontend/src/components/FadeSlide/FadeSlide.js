import { Swiper, SwiperSlide } from "swiper/react";
import { EffectFade } from "swiper/modules";
import { useRef, useState, useEffect } from "react";
import "swiper/css/effect-fade";
import { getTopMovieAPI } from '../../service/TheMovieService';
import "./SlideShow.scss"; // chứa style custom
import CustomizeButton from "../CustomeButton";
import { useNavigate } from "react-router-dom";
import { data } from './fakeData'

function FadeSlide() {
    const swiperRef = useRef(null);
    const navigate = useNavigate();
    const [initSlide, setSlides] = useState(data);
    const [activeIndex, setActiveIndex] = useState(0);


    useEffect(() => {
        async function fetchData() {
            try {
                const response = await getTopMovieAPI();
                if (response.result.length > 1) {
                    setSlides(response.result);
                }
            } catch (error) {
                setSlides(data);
            }
        }
        fetchData();
    }, [])

    return (
        <div className="hero-slider">
            <Swiper
                modules={ [EffectFade] }
                effect="fade"
                slidesPerView={ 1 }
                onSwiper={ (swiper) => (swiperRef.current = swiper) }
                onSlideChange={ (swiper) => setActiveIndex(swiper.activeIndex) }
            >
                { initSlide.map((item) => (
                    <SwiperSlide key={ item.id }>
                        <div
                            className="hero-slide"
                            style={ {
                                backgroundImage: `url(${item.largeImage})`,
                                cursor: 'grab'
                            } }
                        >
                            <div className='opacit'>
                                <div className="none-display" onClick={ () => navigate(`/movie-detail/${item.id}`) } ></div>
                                <div className="dot-grid">
                                    <div className='content-slide' >
                                        <div style={ { cursor: 'pointer' } } onClick={ () => navigate(`/movie-detail/${item.id}`) }>
                                            <div className='infor-movie'>
                                                <strong className='text-red'>Đang Chiếu</strong>
                                                <p className='genre-text'>Hành động, Trinh thám</p>
                                            </div>
                                            <div className='name-slide'>{ item.nameVN }</div>
                                        </div>
                                        <p className="text-red pb-2 fs-7">{ item.nameEN || "Avenger" }</p>
                                        <div className='d-flex align-items-center detail'>
                                            <p className="border-1 border-light rounded-1 bg-light text-black fw-bold">{ item.ageLimit }+</p>
                                            <p className='btn btn-sm text-light border border-1 border-gold pe-3 ps-3'>2025</p>
                                            <p className="btn btn-sm text-light border-1 bg-light text-black pe-3 ps-3"><i className="fa-regular fa-clock"></i> { item.duration } phút</p>
                                        </div>
                                        <div className='des line-clamp'>{ item.content }</div>

                                        <div className='d-flex align-items-center'>
                                            <CustomizeButton
                                                className="login-btn kj shadow-hover-gold"
                                                large rounded
                                                leftIcon={ <i className="fa-solid fa-ticket"></i> }
                                            >
                                                Đặt vé
                                            </CustomizeButton>
                                            <CustomizeButton
                                                outLine
                                                rounded
                                                large
                                                onClick={ () => navigate(`/movie-detail/${item.id}`) }
                                                className="kj"
                                            >
                                                Xem chi tiết
                                            </CustomizeButton>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </SwiperSlide>
                )) }
            </Swiper>

            {/* Thumbnail Pagination */ }
            <div className="thumbnail-navigation">
                { initSlide.map((item, index) => (
                    <img
                        key={ item.id }
                        src={ item.largeImage }
                        alt={ item.nameVN }
                        onClick={ () => swiperRef.current.slideTo(index) }
                        className={ `thumbnail ${index === activeIndex ? "border-2 border-light" : ""}` }
                    />
                )) }
            </div>

        </div>
    );
};

export default FadeSlide