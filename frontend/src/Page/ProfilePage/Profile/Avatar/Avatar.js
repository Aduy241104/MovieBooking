import { Modal } from 'antd';
import { useState } from 'react';


function Avatar({ originalImage }) {
    const [isOpen, setOpen] = useState(false);

    return (
        <>
            <Modal
                open={ isOpen }
                onCancel={ () => setOpen(false) }
                footer={ null }
                title={ null }
                className="trailer-modal"
                closeIcon={ false }
                width={ 600 }
            >
                <div className='w-100 bg-light'>
                    <h1>Hello</h1>
                </div>
            </Modal>

            <div
                className='d-flex flex-column justify-content-center align-items-center cursor-pointer'
                style={ { marginTop: '210px', marginRight: "190px" } }
            >
                <div
                    onClick={ () => setOpen(true) }
                    style={ { width: '116px', height: '116px', overflow: 'hidden', borderRadius: '90%' } }
                    className='border-2'
                >
                    <img
                        style={ { width: '100%', objectFit: 'cover' } }
                        src={ originalImage } alt="Avatar"
                    />
                </div>
                <p className='mt-3 fw-bold fs-8'> Đổi ảnh đại diện</p>
            </div>
        </>
    )
}

export default Avatar