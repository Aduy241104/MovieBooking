import Tippy from '@tippyjs/react/headless'
import { memo, useEffect, useRef, useState } from 'react'
import 'tippy.js/dist/tippy.css'
import styles from './Search.module.scss'
import classNames from 'classnames/bind'
import useDebounce from '../../../hooks/useDebounce'
import { searchMovieByName } from '../../../service/TheMovieService'
import { useNavigate } from 'react-router-dom'

const cx = classNames.bind(styles);

function Search() {
    const [isShow, setShow] = useState(false);
    const [isLoading, setLoading] = useState(false);
    const [searchValue, setSearchValue] = useState("");
    const [searchResult, setSearchResult] = useState([]);
    const [page, setPage] = useState({
        currentPage: 0,
        totalPage: 0
    });
    const naviagate = useNavigate();
    const inputRef = useRef(null);

    // debounce hook to delay API
    const debounceValue = useDebounce(searchValue, 1000);

    //update stata for search value
    const handleChangeSearchValue = (e) => {
        const value = e.target.value;
        if (value.length > 0 && value.trim() === "") {
            return;
        } else if (value === "") {
            setSearchResult([])
        }
        setSearchValue(value);
    }

    // clear keyword on seacrh bar and clear search result
    const handleClearSearchValue = () => {
        setSearchValue("");
        setSearchResult([]);
    }

    // use to load more search result
    const handleShowMore = () => {
        if (page.currentPage + 1 < page.totalPage) {
            setPage(prev => ({ ...prev, currentPage: prev.currentPage + 1 }));
        }
    }

    // fetch data from server using axios 
    const fetchData = async () => {
        setLoading(true);
        try {
            const response = await searchMovieByName(searchValue, page.currentPage, 4);
            const { Movie } = response.result;
            const { total_page } = response.result.Meta;

            setSearchResult(prev => [...prev, ...Movie]);
            setPage(prev => ({ ...prev, totalPage: total_page }));

        } catch (error) {
            console.log(error);
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        if (!!debounceValue.trim()) {
            setSearchResult([]);
            setPage({ currentPage: 0, totalPage: 0 })
            fetchData();
        }
    }, [debounceValue])


    useEffect(() => {
        if (!!debounceValue.trim()) {
            fetchData();
        }
    }, [page.currentPage])

    //focus input when open
    useEffect(() => {
        if (isShow && inputRef.current) {
            inputRef.current.focus();
        }
    }, [isShow]);

    return (
        <>
            { isShow && <div className={ cx('overlay') }></div> }
            <div>
                <Tippy
                    interactive={ true }
                    visible={ isShow }
                    placement="top-end"
                    offset={ [24, 20] }
                    onClickOutside={ () => { setShow(false); handleClearSearchValue() } }
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
                                    ref={ inputRef }
                                    className={ cx('search-input', 'pe-2 ps-2 border-0 flex-fill') }
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
                                    { searchResult.map((item) => {
                                        return (
                                            <div
                                                className={ cx('search-result-layout', 'red-hover', 'd-flex mt-3 border-bottom border-lightGray pb-2') }
                                                key={ item.id }
                                                onClick={ () => naviagate(`/movie-detail/${item.id}`) }
                                            >
                                                <div className='w-25'>
                                                    <img
                                                        src={ item.smallImage }
                                                        alt=""
                                                        loading="lazy"
                                                        onError={ (e) => {
                                                            e.target.onerror = null; // Ngăn lặp vô hạn nếu ảnh fallback cũng lỗi
                                                            e.target.src = "https://upload.wikimedia.org/wikipedia/commons/1/14/No_Image_Available.jpg"; // Đường dẫn ảnh mặc định
                                                        } }
                                                    />
                                                </div>
                                                <div className='ms-2 w-75'>
                                                    <strong className={ cx("movie-name") }>{ item.nameVN }</strong>
                                                    <p className={ cx('genre', 'text-secondary') }>{ item.types.join(', ') }</p>
                                                    <p className='fs-7'><i className="fa-regular fa-clock"></i> { item.duration }m</p>
                                                </div>
                                            </div>
                                        )
                                    }) }

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

export default memo(Search)