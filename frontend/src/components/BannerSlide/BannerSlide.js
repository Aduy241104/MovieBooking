import React, { useEffect, useState } from 'react'
import './Slide.css'
import CustomizeButton from '../CustomeButton';
import { getTopMovieAPI } from '../../service/TheMovieService';

const data = [
    {
        id: 1,
        largeImage: '/img/wp2576681-doraemon-and-friends-3d-wallpaper.jpg',
        nameVN: "Mức cát-xê",
        content: "Robert Downey Jr. (Iron Man) với mức tiền khổng lồ vì là trụ cột suốt nhiều năm của Marvel"

    },
    {
        id: 2,
        largeImage: 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/m/i/mi8_poster_1800x1200.jpg',
        nameVN: "Nhiệm vụ bất khả thi",
        content: "Robert Downey Jr. (Iron Man) với mức tiền khổng lồ vì là trụ cột suốt nhiều năm của Marvel"

    },
    {
        id: 3,
        largeImage: 'https://cdn.galaxycine.vn/media/2025/3/18/toi-do-750_1742267857580.jpg',
        nameVN: "Tội Đồ",
        content: "Robert Downey Jr. (Iron Man) với mức tiền khổng lồ vì là trụ cột suốt nhiều năm của Marvel"

    },
    {
        id: 4,
        largeImage: 'https://www.bhdstar.vn/wp-content/uploads/2025/05/referenceSchemeHeadOfficeallowPlaceHoldertrueheight360ldapp-1.png',
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

function BannerSlide() {
    const [initSlide, setSlides] = useState(data);


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

    const handleNext = () => {
        setSlides(prev => [...prev.slice(1), prev[0]]);
    };

    const handlePrev = () => {
        setSlides(prev => [prev[prev.length - 1], ...prev.slice(0, prev.length - 1)]);
    };

    return (
        <div className='container-Slide p-4'>
            <div className='slide'>
                { initSlide.map((item) => {
                    return (
                        <div className='item-slide' style={ { backgroundImage: `url(${item.largeImage})` } } key={ item.id }>
                            <div className='opacit'>
                                <div className='content-slide'>
                                    <div className='infor-movie'>
                                        <strong className='text-red'>Sắp Chiếu</strong>
                                        <p className='genre-text'>Hành động, Trinh thám</p>
                                    </div>
                                    <div className='name-slide'>{ item.nameVN }</div>
                                    <div className='d-flex align-items-center detail'>
                                        <p>{ item.ageLimit }+</p>
                                        <p className='btn btn-sm text-light border border-1 border-gold pe-3 ps-3'>2025</p>
                                        <p><i className="fa-regular fa-clock"></i>{ item.duration } phút</p>
                                    </div>
                                    <div className='des line-clamp'>{ item.content }</div>
                                    <div className='d-flex align-items-center'>
                                        <CustomizeButton
                                            className="login-btn shadow-hover-gold"
                                            large rounded
                                            leftIcon={ <i className="fa-solid fa-ticket"></i> }
                                        >
                                            Đặt vé
                                        </CustomizeButton>
                                        <CustomizeButton
                                            outLine
                                            rounded
                                            large
                                        >
                                            Xem chi tiết
                                        </CustomizeButton>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )
                }) }
            </div>
            <div className='button-slide'>
                <button className='prev' onClick={ handlePrev }><i className="fa-solid fa-arrow-left"></i></button>
                <button className='next' onClick={ handleNext }><i className="fa-solid fa-arrow-right"></i></button>
            </div>
        </div>
    )
}

export default BannerSlide