import classNames from 'classnames/bind'
import styles from './MovieItem.module.scss'

const cx = classNames.bind(styles);

function MovieItem() {
    return (
        <div className='d-flex w-100 pb-4'>
            <div className={ cx('poster') }>
                <img
                    src="https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/image/1800x/71252117777b696995f01934522c402d/z/o/zootopia_2_-_teaser_poster_up.jpg"
                    alt=""
                />
            </div>

            <div className='ms-3 w-75 cursor-pointer'>
                <p className='fs-7'>9.5 <i className="fa-solid fa-star text-warning"></i></p>
                <strong className='fs-5'>Nhiệm Vụ: Bất Khả Thi - Nghiệp Báo Cuối Cùng</strong>
                <p className='text-secondary'>Mission: Impossible - The Final Reckoning</p>
                <p className='text-secondary fs-7'>Gay cấn, Phiêu lưu, Hành động</p>

                <div className='d-flex flex-wrap gap-2 mt-3 w-100'>
                    <button className='border-1 p-1 pe-2 ps-2 rounded-2 fs-7 red-hover me-1'>
                        <span className='fw-bold fs-6'>14:50</span> - 16:35
                    </button>

                    <button className='border-1 p-1 pe-2 ps-2 rounded-2 fs-7 red-hover me-1'>
                        <span>14:50</span> - 16:35
                    </button>
                    <button className='border-1 p-1 pe-2 ps-2 rounded-2 fs-7 red-hover me-1'>
                        <span>14:50</span> - 16:35
                    </button>

                </div>
            </div>
        </div>
    )
}

export default MovieItem