import { useEffect, useLayoutEffect, useState } from 'react'
import DefaultLayout from '../../layouts/DefaultLayout'
import { useNavigate, useParams } from 'react-router-dom'
import styles from './MovieDetail.module.scss'
import classNames from 'classnames/bind'
import RightComponent from './RightComponent'
import { getMovieDetailAPI } from '../../service/TheMovieService'

const cx = classNames.bind(styles);
const DEFAULT_BG =
    'https://image.tmdb.org/t/p/original/xMgfvjhKwuFTyoXNpa8UVV74rek.jpg';

function MovieDetail() {
    const { id } = useParams();
    const [movie, setMovie] = useState({});
    const [errorMessage, setError] = useState("");
    const [bgUrl, setBgUrl] = useState(DEFAULT_BG);

    useLayoutEffect(() => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }, []);


    useEffect(() => {
        const fetchMovieData = async (id) => {
            try {
                const response = await getMovieDetailAPI(id);
                setMovie(response.result);

                const path = response.result?.largeImage;
                if (path?.trim()) {
                    const fullUrl = path;
                    const img = new Image();
                    img.src = fullUrl;

                    img.onload = () => setBgUrl(fullUrl);
                    img.onerror = () => setBgUrl(DEFAULT_BG);
                } else {
                    setBgUrl(DEFAULT_BG);
                }
            } catch (error) {
                console.error(error);
                setBgUrl(DEFAULT_BG);
            }
        };
        fetchMovieData(id);
    }, [id])

    return (
        <>
            <DefaultLayout>
                <div
                    className={ cx('background-img') }
                    style={ {
                        backgroundImage: `url("${bgUrl}")`,
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
                                        <img src={ movie.smallImage }
                                            alt=""
                                            className={ cx('poster-small') }
                                        />
                                    </div>
                                    <div className='mt-4 w-75'>
                                        <h3>{ movie.nameVN }</h3>
                                        <p className='text-red fs-7 mt-3'>Yadang: The Snitch</p>
                                        <div className='d-flex flex-wrap gap-2 mt-4 mb-3'>
                                            <div className='w-100 pb-2'>
                                                <span className='p-1 pe-3 ps-3 border-1 border-gold fs-8 rounded-1'>2025</span>
                                            </div>
                                            { movie.types && movie.types.map((item, index) => {
                                                return (
                                                    <button key={ index } className={ cx('movie-genre') }>{ item }</button>
                                                )
                                            }) }
                                        </div>
                                        <div className='mt-3'>
                                            <strong>Giới thiệu: </strong>
                                            <p className={ cx('fs-7 mt-2', 'text-gray') }>
                                                { movie.content }
                                            </p>
                                        </div>
                                        <div className='mt-4'>
                                            <ul className='p-0'>
                                                <li className='fs-7 pb-3' >
                                                    <strong>Thời lượng: </strong>
                                                    <span className={ cx('text-gray') }>1h 39m</span>
                                                </li>
                                                <li className='fs-7 pb-3' >
                                                    <strong>Đạo diễn: </strong>
                                                    <span className='fw-300'>Warner Bros</span>
                                                </li>
                                                <li className='fs-7 pb-3' >
                                                    <strong>Sản xuất: </strong>
                                                    <span className='fw-300'>Warner Bros. Animation, DC Entertainment</span>
                                                </li>
                                                <li className='fs-7 pb-3' >
                                                    <strong>Sản xuất: </strong>
                                                    <span className='fw-300'>Warner Bros. Animation, DC Entertainment</span>
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