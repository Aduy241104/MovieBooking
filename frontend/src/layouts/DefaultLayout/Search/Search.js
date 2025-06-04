import Tippy from '@tippyjs/react/headless'
import { useState } from 'react'
import 'tippy.js/dist/tippy.css'
import styles from './Search.module.scss'
import classNames from 'classnames/bind'

const cx = classNames.bind(styles);

function Search({ children }) {
    const [isShow, setShow] = useState(false);
    const [isLoading, setLoading] = useState(false);

    return (
        <>
            { isShow && <div className={ cx('overlay') }></div> }
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

                        <div className={ cx('d-flex align-items-center p-2 border border-2 border-secondary rounded-2', 'search-place') }>
                            <i className="fa-solid fa-magnifying-glass text-dark"></i>
                            <input type="text" placeholder='Kiếm gì đê' className={ cx('search-input', 'pe-2 ps-2 border-0') } />
                        </div>

                        { (!isLoading) ? (
                            <div>
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
                <button onClick={ () => setShow(true) } className="bg-transparent me-3 border-0">
                    <i className="fa-solid fa-magnifying-glass text-light"></i>
                </button>
            </Tippy>
        </>
    )
}

export default Search