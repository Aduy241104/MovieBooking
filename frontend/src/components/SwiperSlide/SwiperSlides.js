import 'swiper/css';
import 'swiper/css/free-mode';
import 'swiper/css/pagination';
import 'swiper/css/navigation';

import { Swiper } from 'swiper/react';
import { FreeMode, Navigation } from 'swiper/modules';


function SwiperSlides({ children }) {
    return (
        <>
            <Swiper
                spaceBetween={ 20 }
                grabCursor={ true }
                freeMode={ true }
                navigation={ true }
                modules={ [Navigation, FreeMode] }
                breakpoints={ {
                    992: {
                        slidesPerView: 5, // Desktop
                    },
                    0: {
                        slidesPerView: 'auto', // Mobile: tự co dãn theo chiều ngang
                    },
                } }
                className="mySwiper"
            >
                { children }
            </Swiper>
        </>
    );
}

export default SwiperSlides