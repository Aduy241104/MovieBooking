import { Modal, Avatar, Divider, Upload, message } from "antd";
import { UploadOutlined, UserOutlined } from "@ant-design/icons";
import React, { useState } from "react";

export const UploadAvatarModal = ({
    open,
    onCancel,
    uploading,
    currentAvatar,
    onUpload,
}) => {
    const [fileList, setFileList] = useState([]);
    const [previewImage, setPreviewImage] = useState('');

    // Validate file trước khi upload
    const beforeUpload = (file) => {
        const isJpgOrPng = file.type === 'image/jpeg'
            || file.type === 'image/png'
            || file.type === 'image/jpg';
        if (!isJpgOrPng) {
            message.error('Chỉ hỗ trợ file JPG/PNG!');
            return Upload.LIST_IGNORE;
        }
        const isLt5M = file.size / 1024 / 1024 < 5;
        if (!isLt5M) {
            message.error('Ảnh phải nhỏ hơn 5MB!');
            return Upload.LIST_IGNORE;
        }
        return false;
    };

    // Xử lý khi chọn file
    const handleFileChange = (info) => {
        const newFileList = info.fileList;
        const validFileList = newFileList.filter(file => {
            if (file.status === 'error') return false;
            if (file.originFileObj) {
                const isValidType = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.originFileObj.type);
                const isValidSize = file.originFileObj.size / 1024 / 1024 < 5;
                return isValidType && isValidSize;
            }
            return true;
        });
        setFileList(validFileList);

        if (validFileList.length > 0 && validFileList[0].originFileObj) {
            const reader = new FileReader();
            reader.readAsDataURL(validFileList[0].originFileObj);
            reader.onload = (e) => setPreviewImage(e.target.result);
        } else {
            setPreviewImage('');
        }
    };

    // Khi nhấn OK
    const handleOk = () => {
        if (fileList.length === 0) {
            message.warning('Vui lòng chọn ảnh để upload!');
            return;
        }
        onUpload(fileList[0].originFileObj, () => {
            setFileList([]);
            setPreviewImage('');
        });
    };

    // Khi đóng modal
    const handleCancel = () => {
        setFileList([]);
        setPreviewImage('');
        onCancel();
    };

    return (
        <Modal
            title="Cập nhật ảnh đại diện"
            open={open}
            onOk={handleOk}
            onCancel={handleCancel}
            okText="Cập nhật"
            cancelText="Hủy"
            confirmLoading={uploading}
            width={600}
        >
            <div className="space-y-4">
                <div className="flex justify-center items-center gap-8">
                    {/* Ảnh cũ */}
                    <div className="text-center">
                        <div className="mb-3">
                            <span className="text-gray-600 font-medium">Ảnh hiện tại</span>
                        </div>
                        <Avatar
                            size={120}
                            src={`${process.env.REACT_APP_BACKEND_URL}/avatars/${currentAvatar}`}
                            icon={!currentAvatar && <UserOutlined />}
                            style={{ border: '2px solid #f0f0f0' }}
                        />
                    </div>
                    {/* Mũi tên thay đổi */}
                    {previewImage && (
                        <div className="flex flex-col items-center">
                            <div className="text-2xl text-blue-500 mb-2">→</div>
                            <span className="text-xs text-gray-500">Thay đổi</span>
                        </div>
                    )}
                    {/* Ảnh preview */}
                    {previewImage && (
                        <div className="text-center">
                            <div className="mb-3">
                                <span className="text-gray-600 font-medium">Ảnh mới</span>
                            </div>
                            <div className="relative inline-block">
                                <Avatar
                                    size={120}
                                    src={previewImage}
                                    style={{ border: '2px solid #52c41a' }}
                                />
                                <div className="absolute -top-2 -right-2 w-8 h-8 bg-green-500 rounded-full flex items-center justify-center">
                                    <span className="text-white text-sm font-bold">✓</span>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
                <Divider />
                <div>
                    <div className="mb-3">
                        <span className="text-gray-600 font-medium">Chọn ảnh mới:</span>
                        <div className="text-xs text-gray-400 mt-1">
                            Hỗ trợ: JPG, PNG • Tối đa: 5MB
                        </div>
                    </div>
                    <Upload.Dragger
                        name="avatar"
                        listType="picture"
                        fileList={fileList}
                        onChange={handleFileChange}
                        beforeUpload={beforeUpload}
                        maxCount={1}
                        accept="image/jpeg,image/jpg,image/png"
                        className="upload-area"
                        showUploadList={{
                            showRemoveIcon: true,
                            showPreviewIcon: false
                        }}
                    >
                        <p className="ant-upload-drag-icon">
                            <UploadOutlined style={{ fontSize: '48px', color: '#1890ff' }} />
                        </p>
                        <p className="ant-upload-text">Click hoặc kéo thả ảnh vào đây</p>
                        <p className="ant-upload-hint">
                            Chỉ hỗ trợ JPG, PNG • Tối đa 5MB
                        </p>
                    </Upload.Dragger>
                </div>
                {previewImage && (
                    <div className="bg-green-50 border border-green-200 rounded-lg p-3">
                        <div className="flex items-center gap-2">
                            <div className="w-4 h-4 bg-green-500 rounded-full flex items-center justify-center">
                                <span className="text-white text-xs">✓</span>
                            </div>
                            <span className="text-green-700 text-sm font-medium">
                                Ảnh đã sẵn sàng để cập nhật
                            </span>
                        </div>
                    </div>
                )}
            </div>
        </Modal>
    );
};