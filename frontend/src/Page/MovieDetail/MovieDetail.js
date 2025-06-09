import React from 'react'
import DefaultLayout from '../../layouts/DefaultLayout'
import { useNavigate, useParams } from 'react-router-dom'
import styles from './MovieDetail.module.scss'
import classNames from 'classnames/bind'

const cx = classNames.bind(styles);

function MovieDetail() {
    const { id } = useParams();
    return (
        <>
            <DefaultLayout>
                <div className={ cx('poster-large', 'bg-black') }
                    style={ { backgroundImage: 'url("https://kenh14cdn.com/203336854389633024/2025/5/17/yadanglaunchtextless-1-1747451299547290296940-1747457580598-17474575819071505578298.png")' } }
                >
                    <div className={ cx('opacity') }>

                    </div>
                </div>

                <div className='container-fluid text-light'>
                    <div className='row pb-5'>
                        <div className={ cx('col-4 ps-5', 'left-box') }>
                            <div className='mt-4'>
                                <img src="https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/t/e/teaser_poster_1_3.jpg"
                                    alt=""
                                    className={ cx('poster-small') }
                                />
                            </div>

                            <div className='mt-4 w-75'>
                                <h3>Yadang: Ba Mặt Lật Kèo</h3>
                                <p className='text-red fs-7 mt-2'>Yadang: The Snitch</p>
                                <div className='d-flex'>
                                    <button className='fs-7 me-2'>Hành Động</button>
                                    <button>Trinh thám</button>
                                </div>
                                <div className='mt-3'>
                                    <strong>Giới thiệu: </strong>
                                    <p className='text-secondary fs-7 mt-2'>"Từ giờ trở đi, bạn là kẻ chỉ điểm của tôi."
                                        Là “cầu nối” giữa thế giới ngầm và các cơ quan thực thi pháp luật, những kẻ chỉ điểm
                                        chuyên nghiệp được gọi là "yadang" - người cung cấp thông tin bí mật về thế giới ma túy
                                        cho các công tố viên và cảnh sát. Khi một kẻ chỉ điểm ma túy “báo tin” về một bữa tiệc có sự tham dự
                                        của các VIP nổi tiếng và vô tình vướng vào một âm mưu nguy hiểm, hắn phải làm mọi thứ trong khả năng
                                        của mình không chỉ để sống sót,mà còn để phục thù
                                    </p>
                                </div>

                                <div className='mt-3'>


                                </div>
                            </div>
                        </div>

                        <div className={ cx('col-8', 'right-box') }>
                            {/* Nội dung bên phải */ }
                        </div>
                    </div>
                </div>
            </DefaultLayout>
        </>
    )
}

export default MovieDetail