import React, { useEffect, useState } from 'react'
import './Slide.css'
import CustomizeButton from '../CustomeButton';

const data = [
    {
        id: 1,
        ImageUrl: '/img/wp2576681-doraemon-and-friends-3d-wallpaper.jpg',
        Title: "Mức cát-xê",
        Description: "Robert Downey Jr. (Iron Man) với mức tiền khổng lồ vì là trụ cột suốt nhiều năm của Marvel"

    },
    {
        id: 2,
        ImageUrl: 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/m/i/mi8_poster_1800x1200.jpg',
        Title: "Nhiệm vụ bất khả thi",
        Description: "Robert Downey Jr. (Iron Man) với mức tiền khổng lồ vì là trụ cột suốt nhiều năm của Marvel"

    },
    {
        id: 3,
        ImageUrl: 'https://cdn.galaxycine.vn/media/2025/3/18/toi-do-750_1742267857580.jpg',
        Title: "Tội Đồ",
        Description: "Robert Downey Jr. (Iron Man) với mức tiền khổng lồ vì là trụ cột suốt nhiều năm của Marvel"

    },
    {
        id: 4,
        ImageUrl: 'https://www.bhdstar.vn/wp-content/uploads/2025/05/referenceSchemeHeadOfficeallowPlaceHoldertrueheight360ldapp-1.png',
        Title: "",
        Description: "Robert Downey Jr. (Iron Man) với mức tiền khổng lồ vì là trụ cột suốt nhiều năm của Marvel"

    },
    {
        id: 5,
        ImageUrl: 'https://i.ytimg.com/vi/1sfhCvsEHVo/maxresdefault.jpg',
        Title: "YADANG: BA MẶT LẬT KÈO",
        Description: `"Từ giờ trở đi, bạn là kẻ chỉ điểm của tôi." Là “cầu nối” giữa thế giới ngầm và các cơ quan thực thi pháp luật, những kẻ chỉ điểm chuyên nghiệp được gọi là "yadang" - người cung cấp thông tin bí mật về thế giới ma túy cho các công tố viên và cảnh sát. Khi một kẻ chỉ điểm ma túy “báo tin” về một bữa tiệc có sự tham dự của các VIP nổi tiếng và vô tình vướng vào một âm mưu nguy hiểm, hắn phải làm mọi thứ trong khả năng của mình không chỉ để sống sót,mà còn để phục thù.`
    }
]

function BannerSlide() {
    const [initSlide, setSlides] = useState(data);

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
                        <div className='item-slide' style={ { backgroundImage: `url(${item.ImageUrl})` } } key={ item.id }>
                            <div className='opacit'>
                                <div className='content-slide'>
                                    <div className='infor-movie'>
                                        <strong className='text-red'>Sắp Chiếu</strong>
                                        <p className='genre-text'>Hành động, Trinh thám</p>
                                    </div>
                                    <div className='name-slide'>{ item.Title }</div>
                                    <div className='d-flex align-items-center detail'>
                                        <p><i className="fa-solid fa-star text-warning"></i> 8.4</p>
                                        <p className='btn btn-sm text-light border border-1 border-gold pe-3 ps-3'>2025</p>
                                        <p><i className="fa-regular fa-clock"></i>150 min</p>
                                    </div>
                                    <div className='des line-clamp'>{ item.Description }</div>
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