import { Modal } from 'antd';

function TrailerModal({ open, onClose, trailerUrl }) {
    return (
        <Modal
            title="Trailer phim"
            open={ open }
            onCancel={ onClose }
            footer={ null }
            width={ 800 }
        >
            <div style={ { position: 'relative', paddingBottom: '56.25%', height: 0 } }>
                <iframe
                    title="Trailer"
                    width="100%"
                    height="100%"
                    style={ { position: 'absolute', top: 0, left: 0 } }
                    src={ trailerUrl }
                    allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                />
            </div>
        </Modal>
    )
}

export default TrailerModal