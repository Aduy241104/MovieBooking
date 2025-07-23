import { useDispatch, useSelector } from "react-redux";
import { useParams } from "react-router-dom";
import DefaultLayout from "../../layouts/DefaultLayout";
import { Dropdown, Button } from 'antd';
import { DownOutlined } from '@ant-design/icons';
import MovieComp from "../../components/MovieComp/MovieComp";
import { useEffect, useState } from "react";
import getGenreList from './genre'
import { fetchComingSoon, fetchNowPlaying } from "../../redux/slices/movieSlice";



function MovieListPage() {

    const { type } = useParams();
    const [genre, setGenre] = useState("Tất cả");
    const [ageLimit, setAgeLimit] = useState("Tất cả");
    const listMovie = useSelector((state) => {
        return type === "now-playing" ? state.movie.nowPlaying : state.movie.comingSoon;
    });
    const dispatch = useDispatch();

    const filterMovies = (movieList, filters) => {
        return movieList.filter(movie => {
            const isMatchedGenre = filters.genre === "Tất cả" || movie.types.includes(filters.genre);
            const isMatchedAgeLimit = filters.ageLimit === "Tất cả" || movie.ageLimit <= parseInt(filters.ageLimit);

            return isMatchedAgeLimit && isMatchedGenre;
        })
    }


    const hanleChangeGenreFilter = (typeFilter) => {
        setGenre(typeFilter);
    }

    useEffect(() => {
        if (listMovie.status === "idle") {
            dispatch(type === "now-playing" ? fetchNowPlaying() : fetchComingSoon());
        }
    }, [listMovie.status])


    return (
        <DefaultLayout>
            <div className="pt-5 text-light">
                <div
                    className="mt-3 d-flex flex-column justify-content-center align-items-center"
                    style={ { backgroundImage: 'url("/img/tix-banner.ed8b6071.png")', height: "140px" } }
                >
                    <h4>{ type == "now-playing" ? "Đang chiếu" : "Sắp chiếu" }</h4>
                    <p className="pt-2">
                        Danh sách các phim hiện { type == "now-playing" ? "đang chiếu" : "sắp chiếu"} rạp trên toàn quốc { new Date().toLocaleDateString('vi-VN') } — Xem lịch chiếu phim, giá vé tiện lợi, đặt vé nhanh chỉ với 1 bước!
                    </p>

                </div>
                <div className="container p-5">
                    <div className="row pe-5 ps-5">
                        <div className="col-md-3 col-12">
                            <Dropdown menu={ getGenreList(hanleChangeGenreFilter) } trigger={ ['click'] }>
                                <Button style={ { width: '185px' } } className="p-4 ps-5 pe-5 fw-bold fs-6">
                                    { genre }
                                    <DownOutlined />
                                </Button>
                            </Dropdown>

                        </div>
                        <div className="col-md-9 col-12 container-fluid">
                            <div className="row">
                                { filterMovies(listMovie.movies, {
                                    genre: genre,
                                    ageLimit: ageLimit
                                }).map(item => {
                                    return (
                                        <div key={ item.id } className="col-md-3 col-6 mb-4">
                                            <MovieComp
                                                warpperSmall={ true }
                                                imglink={ item.smallImage }
                                                nameVN={ item.nameVN }
                                                types={ item.types }
                                                ageLimit={ item.ageLimit }
                                                id={ item.id }
                                            />
                                        </div>
                                    )
                                }) }
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </DefaultLayout>
    )
}

export default MovieListPage