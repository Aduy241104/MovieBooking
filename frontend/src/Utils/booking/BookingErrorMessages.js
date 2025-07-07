// Một object để "dịch" mã lỗi từ backend sang tiếng Việt
export const promotionErrorMessages = {
    'PROMOTION_INVALID': 'Mã khuyến mãi không hợp lệ hoặc đã hết hạn.',
    'PROMOTION_INVALID_OR_EXPIRED': 'Mã khuyến mãi không hợp lệ hoặc đã hết hạn.',
    'PROMOTION_MIN_ORDER_NOT_MET': (minOrder) => `Tổng tiền chưa đạt mức tối thiểu ${minOrder.toLocaleString('vi-VN')}đ của khuyến mãi.`,
    'POINTS_EXCEEDED': 'Số điểm sử dụng vượt quá số điểm hiện có của bạn.',
    // Thêm các mã lỗi khác nếu có
    'DEFAULT': 'Có lỗi xảy ra, vui lòng thử lại.'
};

/**
 * Hàm helper để lấy và định dạng thông báo lỗi
 * @param {string} rawMessage - Chuỗi message thô từ backend (ví dụ: "PROMOTION_MIN_ORDER_NOT_MET:150000")
 * @returns {string} - Thông báo lỗi đã được dịch sang tiếng Việt
 */
export const getFriendlyErrorMessage = (rawMessage) => {
    if (!rawMessage) return promotionErrorMessages.DEFAULT;

    const parts = rawMessage.split(':');
    const errorCode = parts[0];
    const errorValue = parts[1];

    const messageGenerator = promotionErrorMessages[errorCode];

    if (typeof messageGenerator === 'function') {
        // Nếu là hàm (như trường hợp minOrder), gọi hàm đó với giá trị đi kèm
        return messageGenerator(Number(errorValue));
    } else if (messageGenerator) {
        // Nếu là chuỗi, trả về chuỗi đó
        return messageGenerator;
    } else {
        // Nếu không có mã lỗi nào khớp, trả về chuỗi gốc hoặc một message mặc định
        return rawMessage; 
    }
};