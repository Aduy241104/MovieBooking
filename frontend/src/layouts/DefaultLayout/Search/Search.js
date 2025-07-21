import Tippy from '@tippyjs/react/headless'
import { memo, useEffect, useRef, useState } from 'react'
import 'tippy.js/dist/tippy.css'
import styles from './Search.module.scss'
import classNames from 'classnames/bind'
import useDebounce from '../../../hooks/useDebounce'
import { searchMovieByName } from '../../../service/TheMovieService'
import { useNavigate } from 'react-router-dom'
import SearchHistoryList from './SearchHistoryList/SearchHistoryList'

const cx = classNames.bind(styles);

function Search() {
    const [isShow, setShow] = useState(false);
    const [isLoading, setLoading] = useState(false);
    const [searchValue, setSearchValue] = useState("");
    const [isCleared, setIsCleared] = useState(false);
    const [searchResult, setSearchResult] = useState([]);
    const [showHistorySearch, setShowHistorySearch] = useState(false);
    const [page, setPage] = useState({
        currentPage: 0,
        totalPage: 0
    });
    const naviagate = useNavigate();
    const inputRef = useRef(null);

    // debounce hook to delay API
    const debounceValue = useDebounce(searchValue, 700);

    //update stata for search value
    const handleChangeSearchValue = (keyword) => {
        const value = keyword;
        if (value.length > 0 && value.trim() === "") {
            return;
        } else if (value === "") {
            setSearchResult([])
            setShow(false)
        }

        if (showHistorySearch) {
            setShowHistorySearch(false)
        }
        setLoading(true);
        setSearchValue(value);
    }

    // clear keyword on seacrh bar and clear search result
    const handleClearSearchValue = () => {
        setSearchValue("");
        setSearchResult([]);
        setShow(false);
        // isCleared is a flag to control focus to input after clear search keyword
        setIsCleared(true);
    }

    const handleShowResult = () => {
        if (searchValue) {
            setShow(true);
        } else {
            setShow(true)
            setShowHistorySearch(true);
        }
    }

    // use to load more search result
    const handleShowMore = () => {
        if (page.currentPage + 1 < page.totalPage) {
            setPage(prev => ({ ...prev, currentPage: prev.currentPage + 1 }));
        }
    }

    const saveSearchKeyword = (keyword) => {
        if (!keyword.trim()) {
            return;
        }
        const key = 'searchHistory';
        let currentHistory = JSON.parse(localStorage.getItem(key)) || [];
        // Xoá trùng nếu đã tồn tại
        currentHistory = currentHistory.filter(item => item !== keyword);
        // Thêm vào đầu
        currentHistory.unshift(keyword);
        if (currentHistory.length > 12) {
            currentHistory = currentHistory.slice(0, 12);
        }
        localStorage.setItem(key, JSON.stringify(currentHistory));
    }

    const handleChangePage = (movieId) => {
        saveSearchKeyword(searchValue);
        naviagate(`/movie-detail/${movieId}`);
    }

    // fetch data from server using axios 
    const fetchData = async () => {
        setShow(true)
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

    useEffect(() => {
        if (searchValue === "" && isCleared) {
            inputRef.current?.focus();
            setIsCleared(false); // reset flag
        }
    }, [searchValue, isCleared]);

    return (
        <>
            { isShow && <div className={ cx('overlay') }></div> }
            <div>
                <Tippy
                    interactive={ true }
                    visible={ isShow }
                    placement="top-end"
                    offset={ [1, 6] }
                    onClickOutside={ () => { setShow(false); setShowHistorySearch(false) } }
                    render={ attrs => (
                        <div
                            className={ cx("bg-dark p-3 rounded-1 cursor-pointer", 'arr') }
                            tabIndex="-1"
                            { ...attrs }
                        >

                            { showHistorySearch && <SearchHistoryList changeSearchValue={ handleChangeSearchValue } /> }

                            {/* result search */ }
                            { searchValue.trim() && !showHistorySearch && (
                                !isLoading ? (
                                    <div className={ cx('search-layout', 'custome-scroll-bar', 'd-flex flex-column align-items-center text-light') }>

                                        { searchResult.map((item) => (
                                            <div
                                                className={ cx('search-result-layout', 'gray-hover', 'd-flex mt-3 border-bottom border-black pb-2') }
                                                key={ item.id }
                                                onClick={ () => handleChangePage(item.id) }
                                            >
                                                <div className='w-25'>
                                                    <img
                                                        src={ item.smallImage }
                                                        alt=""
                                                        loading="lazy"
                                                        onError={ (e) => {
                                                            e.target.onerror = null;
                                                            e.target.src = "https://upload.wikimedia.org/wikipedia/commons/1/14/No_Image_Available.jpg";
                                                        } }
                                                    />
                                                </div>
                                                <div className='ms-2 w-75'>
                                                    <strong className={ cx("movie-name") }>{ item.nameVN }</strong>
                                                    <p className={ cx('genre', 'text-secondary') }>{ item.types.join(', ') }</p>
                                                    <p className='fs-7'><i className="fa-regular fa-clock"></i> { item.duration }m</p>
                                                </div>
                                            </div>
                                        )) }

                                        { !!searchResult.length && (
                                            <button className='mt-3 fw-bold fs-7 text-start w-100' onClick={ handleShowMore }>
                                                <i className="fa-solid fa-chevron-down me-1"></i>
                                                Xem thêm
                                            </button>
                                        ) }

                                        { !searchResult.length > 0 && (
                                            <p className='text-center'>Không tìm thấy kết quả nào 🙄</p>
                                        ) }
                                    </div>
                                ) : (
                                    <div className='w-100 d-flex justify-content-center p-5'>
                                        <img src="/img/Animation - 1752043529548.gif" alt="" style={ { width: '60px' } } />
                                    </div>
                                )
                            ) }
                        </div>
                    ) }
                >

                    <div className={ cx('d-flex align-items-center p-2 rounded-3 me-3', 'search-place') }>
                        <i className="fa-solid fa-magnifying-glass text-light"></i>
                        <input
                            type="text"
                            placeholder='Kiếm gì đê'
                            spellCheck={ false }
                            ref={ inputRef }
                            className={ cx('search-input', 'pe-2 ps-2 border-0 flex-fill text-light') }
                            value={ searchValue }
                            onChange={ (e) => handleChangeSearchValue(e.target.value) }
                            onFocus={ () => handleShowResult() }
                        />
                        { (searchValue !== "") ? (
                            <i className="fa-solid fa-circle-xmark text-light cursor-pointer" onClick={ () => handleClearSearchValue() }></i>
                        ) : ("") }
                    </div>
                </Tippy>
            </div>
        </>
    )
}

export default memo(Search)