import { notification } from 'antd';

export const openNotification = (type, message, description) => {
    const isMobile = window.innerWidth <= 768;

    notification[type]({
        message,
        description,
        placement: isMobile ? 'topRight' : 'bottomRight',
    });
};
