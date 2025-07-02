import { Modal } from 'antd';

export const showSessionExpiredModal = () => {
    const modal = Modal.confirm({
        title: 'Phiên đăng nhập đã hết hạn',
        content: 'Vui lòng đăng nhập lại để tiếp tục sử dụng hệ thống.',
        centered: true,
        okText: 'Đăng nhập lại',
        cancelText: 'Đóng',
        onCancel: () => {
            modal.destroy(); // Chỉ đóng modal lại
        }
    });
};
