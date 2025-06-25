import { Modal } from 'antd';

export const showSessionExpiredModal = (onConfirm) => {
    Modal.error({
        title: 'Phiên đăng nhập đã hết hạn',
        content: 'Vui lòng đăng nhập lại để tiếp tục sử dụng hệ thống.',
        centered: true,
        okText: 'Đăng nhập lại',
        onOk: () => {
            if (onConfirm) {
                onConfirm();
            }
        }
    });
};
