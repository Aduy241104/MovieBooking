import classNames from 'classnames/bind'
import styles from './MovieItem.module.scss'

const cx = classNames.bind(styles);

function MovieItem({ data }) {
    return (
        <div className='d-flex w-100 pb-4'>
            <div className={ cx('poster') }>
                <img
                    src={ data.smallImage + "" }
                    alt=""
                    loading="lazy"
                    onError={ (e) => {
                        e.target.onerror = null;
                        e.target.src = "https://upload.wikimedia.org/wikipedia/commons/1/14/No_Image_Available.jpg";
                    } }
                />
            </div>

            <div className='ms-3 w-75 cursor-pointer'>
                <p className='fs-7'>{ data.movie.ageLimit }+</p>
                <strong className='fs-5'>{ data.movie.nameVN }</strong>
                <p className='text-secondary'>{ data.movie.nameEN }</p>
                <p className='text-secondary fs-7'>{ data.types && data.types.join(', ') }</p>

                <div className='d-flex flex-wrap gap-2 mt-3 w-100'>
                    { data.showTime && data.showTime.map((item, index) => {
                        const start = item.showTime.split(":").slice(0, 2).join(":");
                        const end = item.endTime.split(":").slice(0, 2).join(":");

                        return (
                            <button
                                key={ index }
                                className="border-1 p-1 pe-2 ps-2 rounded-2 fs-7 red-hover me-1"
                            >
                                <span className="fw-bold fs-6">{ start }</span> - { end }
                            </button>
                        );
                    }) }

                </div>
            </div>
        </div>
    )
}

export default MovieItem