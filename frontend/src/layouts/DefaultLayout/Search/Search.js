import Tippy from '@tippyjs/react/headless'
import { useEffect, useState } from 'react'
import 'tippy.js/dist/tippy.css'
import styles from './Search.module.scss'
import classNames from 'classnames/bind'
import useDebounce from '../../../hooks/useDebounce'
import { searchMovieByName } from '../../../service/TheMovieService'
import { set } from 'lodash'

const cx = classNames.bind(styles);

function Search({ children }) {
    const [isShow, setShow] = useState(false);
    const [isLoading, setLoading] = useState(false);
    const [searchValue, setSearchValue] = useState("");
    const [searchResult, setSearchResult] = useState([]);
    const [page, setPage] = useState({
        currentPage: 0,
        totalPage: 0
    });

    const debounceValue = useDebounce(searchValue, 1000);

    const handleChangeSearchValue = (e) => {
        const value = e.target.value;
        if (value.length > 0 && value.trim() === "") {
            return;
        }
        console.log(value);
        setSearchValue(value);
    }

    const handleClearSearchValue = () => {
        setSearchValue("");
        setSearchResult([]);
    }

    const handleShowMore = () => {
        if (page.currentPage + 1 < page.totalPage) {
            setPage(prev => {
                return {
                    ...prev,
                    currentPage: prev.currentPage + 1
                }
            })
        }
    }

    const fetchData = async () => {
        setLoading(true);
        try {
            const response = await searchMovieByName(searchValue, page.currentPage, 4);
    
            setSearchResult(prev => {
                return [
                    ...prev,
                    ...response.result.Movie
                ]
            })

            setPage(prev => {
                return {
                    ...prev,
                    totalPage: response.result.Meta.total_page
                }
            })

        } catch (error) {
            console.log(error);
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        if (!!debounceValue.trim()) {
            setSearchResult([]);
            setPage(prev => {
                return {
                    currentPage: 0,
                    totalPage: 0
                }
            })
            fetchData()
        }
    }, [debounceValue])

    useEffect(() => {
        if (!!debounceValue.trim()) {
            fetchData();
        }
    }, [page.currentPage])

    return (
        <>
            { isShow && <div className={ cx('overlay') }></div> }
            <div>
                <Tippy
                    interactive={ true }
                    visible={ isShow }
                    placement="top-end"
                    offset={ [24, 20] }
                    onClickOutside={ () => setShow(false) }
                    render={ attrs => (
                        <div
                            className={ cx("bg-light p-4 rounded-2 cursor-pointer", 'arr') }
                            tabIndex="-1"
                            { ...attrs }
                        >
                            {/* header search layout */ }
                            <div className={ cx('d-flex align-items-center p-2 border border-2 border-secondary rounded-2', 'search-place') }>
                                <i className="fa-solid fa-magnifying-glass text-secondary"></i>
                                <input
                                    type="text"
                                    placeholder='Kiếm gì đê'
                                    className={ cx('search-input', 'pe-2 ps-2 border-0 flex-fill    ') }
                                    value={ searchValue }
                                    onChange={ (e) => handleChangeSearchValue(e) }
                                />
                                { (searchValue !== "") ? (
                                    <i className="fa-solid fa-circle-xmark text-secondary" onClick={ () => handleClearSearchValue() }></i>
                                ) : ("") }
                            </div>


                            {/* result search */ }
                            { (!isLoading) ? (
                                <div className={ cx('search-layout', 'custome-scroll-bar', 'mt-3 d-flex flex-column align-items-center') }>
                                    <div className={ cx('search-result-layout', 'red-hover', 'd-flex mt-3 border-bottom border-secondary pb-2') }>
                                        <img
                                            src="https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/c5f0a1eff4c394a251036189ccddaacd/v/i/virus-main_poster-2.jpg"
                                            alt=""
                                        />
                                        <div className='ms-2'>
                                            <strong className={ cx("movie-name") }>Nhiệm Vụ Bất Khả Thi - Nghiệp Báo Cuổi Cùng</strong>
                                            <p className={ cx('genre', 'text-secondary') }>Phiêu Lưu, Hành Động</p>
                                            <p><i className="fa-solid fa-star text-warning"></i> 9.3</p>
                                        </div>
                                    </div>
                                    { !!searchResult.length &&
                                        <button className='mt-3 text-red fw-bold' onClick={ () => handleShowMore() }>
                                            <i className="fa-solid fa-chevron-down me-1"></i>
                                            Xem thêm
                                        </button>
                                    }
                                </div>
                            ) : (
                                <div className='w-100 d-flex justify-content-center p-5'>
                                    <div className="spinner-border text-danger" role="status">
                                        <span className="visually-hidden">Loading...</span>
                                    </div>
                                </div>
                            ) }
                        </div>
                    ) }
                >
                    <button onClick={ () => setShow(true) } className="bg-transparent me-4 border-0">
                        <i className="fa-solid fa-magnifying-glass text-light"></i>
                    </button>
                </Tippy>
            </div>
        </>
    )
}

export default Search