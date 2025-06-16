import { Modal } from 'antd';
import './TrailerModal.scss'

function TrailerModal({ open, onClose, trailerUrl }) {
    return (
        <Modal
            open={ open }
            onCancel={ onClose }
            footer={ null }
            title={ null }
            className="trailer-modal"
            closeIcon={ false } // ẩn nút mặc định
            width={ 800 }
        >
            <div style={ { position: 'relative', paddingBottom: '56.25%', height: 0 } }>
                <iframe
                    title="Trailer"
                    width="100%"
                    height="430px"
                    style={ { position: 'absolute', top: 0, left: 0, border: 'none' } }
                    src={ trailerUrl }
                    allow="accelerometer; autoplay; encrypted-media; gyroscope;"
                    allowFullScreen
                />

                <div>
                    <h1>Phim</h1>
                </div>
                <button
                    onClick={ onClose }
                    style={ {
                        position: 'absolute',
                        top: -10,
                        right: -10,
                        zIndex: 1000,
                        background: 'rgba(255, 255, 255)',
                        color: 'white',
                        border: 'none',
                        borderRadius: '100px',
                        padding: '6px 11px',
                        cursor: 'pointer',
                    } }
                >
                    <i className="fa-solid fa-xmark text-black"></i>
                </button>
            </div>
        </Modal>


    )
}

export default TrailerModal