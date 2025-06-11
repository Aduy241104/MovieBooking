import React, { useLayoutEffect, useState } from 'react'
import DefaultLayout from '../../layouts/DefaultLayout'
import { useNavigate, useParams } from 'react-router-dom'
import styles from './MovieDetail.module.scss'
import classNames from 'classnames/bind'
import RightComponent from './RightComponent'

const cx = classNames.bind(styles);

function MovieDetail() {
    const { id } = useParams();
    
    useLayoutEffect(() => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth' 
        });
    }, [])
    return (
        <>
            <DefaultLayout>
                <div
                    className={ cx('background-img') }
                    style={ {
                        backgroundImage: 'url("https://image.tmdb.org/t/p/original/xMgfvjhKwuFTyoXNpa8UVV74rek.jpg")',
                    } }
                >
                    <div className={ cx('poster-large') }>
                        <div className={ cx('opacity') }>
                        </div>
                    </div>
                    <div className={ cx('container-fluid text-light bg-midNight', 'hover-face') }>
                        <div className='container mt-4' style={ { zIndex: '999' } }>
                            <div className='row'>
                                <div className={ cx('col-md-4 col-12 ps-5 pt-5', 'left-box') }>
                                    <div>
                                        <img src="https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/e/teaser_poster_1_3.jpg"
                                            alt=""
                                            className={ cx('poster-small') }
                                        />
                                    </div>
                                    <div className='mt-4 w-75'>
                                        <h3>Yadang: Ba Mặt Lật Kèo</h3>
                                        <p className='text-red fs-7 mt-3'>Yadang: The Snitch</p>
                                        <div className='d-flex flex-wrap gap-2 mt-4 mb-3'>
                                            <div className='w-100 pb-2'>
                                                <span className='p-1 pe-3 ps-3 border-1 border-gold fs-8 rounded-1'>2025</span>
                                            </div>
                                            <button className={ cx('movie-genre') }>Hành Động</button>
                                            <button className={ cx('movie-genre') }>Trinh Thám</button>
                                        </div>
                                        <div className='mt-3'>
                                            <strong>Giới thiệu: </strong>
                                            <p className={ cx('fs-7 mt-2', 'text-gray') }>"Từ giờ trở đi, bạn là kẻ chỉ điểm của tôi."
                                                Là “cầu nối” giữa thế giới ngầm và các cơ quan thực thi pháp luật, những kẻ chỉ điểm
                                                chuyên nghiệp được gọi là "yadang" - người cung cấp thông tin bí mật về thế giới ma túy
                                                cho các công tố viên và cảnh sát. Khi một kẻ chỉ điểm ma túy “báo tin” về một bữa tiệc có sự tham dự
                                                của các VIP nổi tiếng và vô tình vướng vào một âm mưu nguy hiểm, hắn phải làm mọi thứ trong khả năng
                                                của mình không chỉ để sống sót,mà còn để phục thù
                                            </p>
                                        </div>
                                        <div className='mt-4'>
                                            <ul className='p-0'>
                                                <li className='fs-7 pb-3' >
                                                    <strong>Thời lượng: </strong>
                                                    <span className={ cx('text-gray') }>1h 39m</span>
                                                </li>
                                                <li className='fs-7 pb-3' >
                                                    <strong>Sản xuất: </strong>
                                                    <span className='fw-300'>Warner Bros. Animation, DC Entertainment</span>
                                                </li>
                                                <li className='fs-7 pb-3' >
                                                    <strong>Đạo diễn: </strong>
                                                    <span className='fw-300'>Jeff Wamester</span>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>

                                <div className={ cx('col-md-8 col-12 pt-5 ps-5 pe-5', 'right-box') }>
                                    <RightComponent />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </DefaultLayout>
        </>
    )
}

export default MovieDetail