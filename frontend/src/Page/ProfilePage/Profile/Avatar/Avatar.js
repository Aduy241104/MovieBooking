import { Modal, Upload, message } from 'antd';
import { InboxOutlined } from '@ant-design/icons';
import { useState } from 'react';
import { imageLink } from './imageLink'
import './Avt.scss'
import uploadCloud from '../../../../Utils/UploadCloud';
import { updateAvatarAPI } from '../../../../service/ProfileService';
import { openNotification } from '../../../../Utils/Notification';
const { Dragger } = Upload;

function Avatar({ originalImage, gender = "Male" }) {
    const [isOpen, setOpen] = useState(false);
    const [typeUpload, setTypeUpload] = useState("upload");
    const [imageGenre, setImageGenre] = useState([]);
    const [indexUpload, setIndexUpload] = useState(0);
    const [file, setFile] = useState({});
    const [previewUrl, setPreviewUrl] = useState(null);
    const [isLoading, setLoading] = useState(false);

    const handleChangeTab = (type, data = []) => {
        setTypeUpload(type);
        setImageGenre(data);
        setPreviewUrl(null);
        setFile(null);
        setIndexUpload(null);
    }

    // xử lý lấy hình ảnh trong danh sách mặc định của hệ thống
    const handleUpdate = async () => {
        try {
            setLoading(true);
            if (indexUpload === 0) {
                return;
            }
            //dựa vào index của ảnh và list hình ảnh ở state 
            // đang được chọn để lọc ra hình đang được chọn
            const data = imageGenre.find((item) => item.id === indexUpload);
            const dataAPI = {
                avatar: data.url
            }
            const response = await updateAvatarAPI(dataAPI);
            // if (!response.success) {
            //     throw new Error(response.message);
            // }
            openNotification("success", "Cập nhật thành công", "Thông tin tài khoản đã được cập nhật.");
            localStorage.setItem("user", JSON.stringify(response.result))
        } catch (error) {
            openNotification("error", "Lỗi cập nhật", error.message);
        } finally {
            setLoading(false);
        }
    }

    // xử lý việc upload hình từ local, set lại state để dùng khi người dùng bấm lưu lại
    const handleUpdateFromLocal = (info) => {
        const file = info.file.originFileObj || info.file; // fallback nếu không có originFileObj
        if (!file) {
            return;
        }
        const previewUrl = URL.createObjectURL(file);
        setPreviewUrl(previewUrl);
        setFile(file);
    };

    const handleChangeAvatar = async () => {
        try {
            setLoading(true);
            const responseUpload = await uploadCloud(file);
            //thực hiện tiếp việc lưu link ảnh vào db sau khi hình đã được đẩy lên cloud
            const url = responseUpload.url;
            const data = {
                avatar: url
            }

            const responseUploadSystem = await updateAvatarAPI(data);

            openNotification("success", "Cập nhật thành công", "Thông tin tài khoản đã được cập nhật.");
            localStorage.setItem("user", JSON.stringify(responseUploadSystem.result))
        } catch (error) {
            openNotification("error", "Lỗi cập nhật", error.message);
        } finally {
            setLoading(false);
        }
    }

    const props = {
        name: 'file',
        multiple: false,
        accept: 'image/*',
        beforeUpload: (file) => {
            const isImage = file.type.startsWith('image/');
            if (!isImage) {
                message.error('Chỉ được upload file ảnh!');
                return Upload.LIST_IGNORE;
            }
            return false;
        },
        onChange: handleUpdateFromLocal,
        maxCount: 1
    };

    return (
        <>
            <Modal
                open={ isOpen }
                onCancel={ () => setOpen(false) }
                footer={ null }
                title={
                    <h5
                        style={ { backgroundColor: "#2a314e", borderTopLeftRadius: '6px', borderTopRightRadius: '6px' } }
                        className="fw-bold p-3 m-0 p-0 text-light"
                    >
                        Đổi ảnh đại diện
                    </h5>
                }
                className="trailer-modal-2 bg-midnight"
                width={ 600 }
            >
                <div className='d-flex flex-column align-items-center w-100 p-4 text-light'>
                    <div className='w-100 ps-2'>
                        <button className='p-3' onClick={ () => handleChangeTab("default", imageLink.korea) }>Hàn Quốc</button>
                        <button className='p-3' onClick={ () => handleChangeTab("default", imageLink.korea) }>Hàn Quốc</button>
                        <button className='p-3' onClick={ () => handleChangeTab("default", imageLink.anime) }>Hoạt hình</button>
                        <button className='p-3' onClick={ () => handleChangeTab("upload") }>Upload</button>
                    </div>
                    { typeUpload === "upload" &&
                        <>
                            <Dragger { ...props } className="p-4 text-light" style={ { backgroundColor: "#2a314e" } }>
                                <p className="ant-upload-drag-icon">
                                    <InboxOutlined />
                                </p>
                                <p className="ant-upload-text text-light">Click hoặc kéo ảnh vào đây để upload</p>
                                <p className="ant-upload-hint text-light">Chỉ hỗ trợ file hình ảnh (.jpg, .png, ...)</p>
                                <img src={ previewUrl } alt="" />
                            </Dragger>
                            <button
                                className='p-1 ps-3 pe-3 rounded-2 bg-red text-black'
                                onClick={ handleChangeAvatar }
                            >
                                Lưu lại
                                { isLoading &&
                                    <div className="spinner-border spinner-border-sm" role="status">
                                        <span className="visually-hidden">Loading...</span>
                                    </div>
                                }
                            </button>
                        </>
                    }

                    { typeUpload === "default" &&
                        <>
                            <ul className=" p-0 w-100 ant-upload-text text-light d-flex justify-content-center flex-wrap">
                                { imageGenre.map((item) => {
                                    return (

                                        <li
                                            className='p-1 cursor-pointer avt-option'
                                            key={ item.id }
                                            onClick={ () => setIndexUpload(item.id) }
                                        >
                                            <div className={ `avt-inner h-100 w-100 ${indexUpload === item.id ? 'avt-active' : ''}` } style={ { backgroundImage: `url("${item.url}")` } }>
                                                <div className={ `oppa h-100 w-100 ${indexUpload === item.id ? 'avt-active' : ''}` }>

                                                </div>
                                            </div>
                                        </li>
                                    )
                                }) }
                            </ul>
                            <div className='w-100 d-flex justify-content-end'>
                                <button
                                    onClick={ handleUpdate }
                                    className='bg-red text-black p-1 pe-3 ps-3 rounded-1 ms-3'
                                >
                                    Lưu lại
                                    { isLoading &&
                                        <div className="spinner-border spinner-border-sm ms-1" role="status">
                                            <span className="visually-hidden">Loading...</span>
                                        </div>
                                    }
                                </button>
                                <button
                                    onClick={ () => setOpen(false) }
                                    className='bg-light text-black p-1 pe-3 ps-3 rounded-1 ms-2 me-4'
                                >
                                    Đóng
                                </button>
                            </div>
                        </> }
                </div>
            </Modal>
            <div
                className='d-flex flex-column justify-content-center align-items-center cursor-pointer layout-avt'
            >
                <div
                    onClick={ () => setOpen(true) }
                    style={ { width: '116px', height: '116px', overflow: 'hidden', borderRadius: '90%' } }
                    className='border-2'
                >
                    <img
                        style={ { width: '100%', objectFit: 'cover', height: '100%' } }
                        src={ originalImage + "" }
                        alt="Avatar"
                        onError={ (e) => {
                            e.target.onerror = null; // Ngăn lặp vô hạn nếu ảnh fallback cũng lỗi
                            e.target.src = (gender === "Male")
                                ? "/img/pngegg.png"
                                : "/img/pngegg (1).png";
                        } }
                    />

                </div>
                <p className='mt-3 fw-bold fs-8'> Đổi ảnh đại diện</p>
            </div>
        </>
    )
}
export default Avatar