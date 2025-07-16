import { Swiper, SwiperSlide } from "swiper/react";
import { EffectFade } from "swiper/modules";
import { useRef, useState, useEffect } from "react";

import "swiper/css/effect-fade";
import { getTopMovieAPI } from '../../service/TheMovieService';
import "./SlideShow.scss"; // chứa style custom
import CustomizeButton from "../CustomeButton";
import { useNavigate } from "react-router-dom";

const data = [
    {
        id: 1,
        largeImage: 'https://static.nutscdn.com/vimg/1920-0/52c2868b732ec596353e302343027a37.webp',
        nameVN: "Mức cát-xê",
        content: "Robert Downey Jr. (Iron Man) với mức tiền khổng lồ vì là trụ cột suốt nhiều năm của Marvel"

    },
    {
        id: 2,
        largeImage: 'https://static.nutscdn.com/vimg/1920-0/ecd581a3c6ebadbad901ae1301e5e1d5.jpg',
        nameVN: "Nhiệm vụ bất khả thi",
        content: "Robert Downey Jr. (Iron Man) với mức tiền khổng lồ vì là trụ cột suốt nhiều năm của Marvel"

    },
    {
        id: 3,
        largeImage: 'https://static.nutscdn.com/vimg/1920-0/7fb03fc7adc8de125e80bc0d67d0e841.webp',
        nameVN: "Tay Đua F1",
        content: "Brad Pitt vào vai một cựu tay đua trở lại với đường đua Công thức 1 cùng APXGP, một đội đua giả tưởng. Đồng hành cùng Pitt trên chặng đường này là Damson Idris, vào vai người đồng đội của anh. Cùng nhau, họ sẽ đối đầu với những tay đua máu mặt trong lĩnh vực thể thao, chinh phục những đỉnh cao mới. F1 dự kiến khởi chiếu vào 27.06.2025."

    },
    {
        id: 4,
        largeImage: 'https://static.nutscdn.com/vimg/1920-0/a62c136aa493ba29aa54ae0834fc9721.webp',
        nameVN: "",
        content: "Robert Downey Jr. (Iron Man) với mức tiền khổng lồ vì là trụ cột suốt nhiều năm của Marvel"

    },
    {
        id: 5,
        largeImage: 'https://kenh14cdn.com/203336854389633024/2025/5/17/yadanglaunchtextless-1-1747451299547290296940-1747457580598-17474575819071505578298.png',
        nameVN: "YADANG: BA MẶT LẬT KÈO",
        content: `"Từ giờ trở đi, bạn là kẻ chỉ điểm của tôi." Là “cầu nối” giữa thế giới ngầm và các cơ quan thực thi pháp luật, những kẻ chỉ điểm chuyên nghiệp được gọi là "yadang" - người cung cấp thông tin bí mật về thế giới ma túy cho các công tố viên và cảnh sát. Khi một kẻ chỉ điểm ma túy “báo tin” về một bữa tiệc có sự tham dự của các VIP nổi tiếng và vô tình vướng vào một âm mưu nguy hiểm, hắn phải làm mọi thứ trong khả năng của mình không chỉ để sống sót,mà còn để phục thù.`
    }
]

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
                onSlideChange={ (swiper) => setActiveIndex(swiper.activeIndex) } // ✅ đúng chỗ
            >
                { initSlide.map((item) => (
                    <SwiperSlide key={ item.id }>
                        <div
                            className="hero-slide"
                            style={ {
                                backgroundImage: `url(${item.largeImage})`,
                            } }
                        >
                            <div className='opacit'>
                                <div className="dot-grid">
                                    <div className='content-slide'>
                                        <div className='infor-movie'>
                                            <strong className='text-red'>Sắp Chiếu</strong>
                                            <p className='genre-text'>Hành động, Trinh thám</p>
                                        </div>
                                        <div className='name-slide'>{ item.nameVN }</div>
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