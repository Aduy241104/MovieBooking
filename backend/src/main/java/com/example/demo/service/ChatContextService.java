//package com.example.demo.service;
//
//import com.example.demo.utils.QuestionAnalyzer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
///**
// * Service chuyên dụng để xây dựng context tối ưu cho chatbot
// * Chỉ lấy dữ liệu thực sự cần thiết dựa trên intent của user
// */
//@Service
//public class ChatContextService {
//
//    @Autowired
//    private OptimizedChatbotDataService optimizedChatbotDataService;
//
//    /**
//     * Xây dựng system prompt và context dành cho AI model (Overloaded method)
//     *
//     * @param userContent Câu hỏi gốc của user
//     * @param info        Thông tin truy vấn đã phân tích
//     * @return Context đầy đủ để gửi cho AI model
//     */
//    public String buildOptimizedContext(String userContent, QuestionAnalyzer.QueryInfo info) {
//        StringBuilder context = new StringBuilder();
//
//        // Base system prompt
//        context.append(
//                "Bạn là nhân viên tư vấn AI của rạp MovieTheater (chỉ có 1 chi nhánh duy nhất, không có nhiều rạp, không hỏi lại về địa điểm).\n");
//
//        // Kiểm tra nếu có intent cụ thể
//        boolean hasSpecificIntent = info.hasAnyIntent();
//
//        if (!hasSpecificIntent) {
//            // Chào hỏi chung, không cần context database
//            context.append("Hãy trả lời ngắn gọn, tự nhiên, thân thiện.\n");
//            context.append("Nếu người dùng cần hỗ trợ về phim, lịch chiếu, đặt vé, hãy hỏi lại để làm rõ nhu cầu.\n");
//        } else {
//            // Có intent cụ thể - thêm quy tắc và context data
//            context.append("QUY TẮC BẮT BUỘC TUÂN THỦ:\n");
//            context.append(
//                    "1. **NGUYÊN TẮC TUYỆT ĐỐI:** CHỈ sử dụng thông tin có trong phần 'DỮ LIỆU LIÊN QUAN' bên dưới\n");
//            context.append(
//                    "2. **NGHIÊM CẤM:** KHÔNG tự bịa ra tên phim, suất chiếu, giá vé, thời gian, địa chỉ hoặc thông tin không có trong dữ liệu\n");
//            context.append(
//                    "3. **KHI THIẾU DỮ LIỆU:** Nếu không có thông tin trong dữ liệu, trả lời chính xác: 'Hiện tại chưa có thông tin này trong hệ thống'\n");
//            context.append(
//                    "4. **QUY ĐỊNH RẠP:** Sau khi đặt vé thành công, khách hàng KHÔNG thể đổi suất chiếu hoặc hủy/hoàn vé\n\n");
//
//            // Lấy context data tối ưu dựa trên intent
//            System.out.println(">>> Analyzing user intent: " + info);
//            String dataContext = optimizedChatbotDataService.buildContextForIntent(info, userContent);
//            System.out.println(">>> Context data for user intent: " + dataContext);
//            if (dataContext.trim().startsWith("Hiện tại chưa có thông tin về phim này")) {
//                return dataContext.trim(); // Trả lời trực tiếp không cần gọi AI
//            }
//
//            if (!dataContext.isEmpty()) {
//                context.append("DỮ LIỆU LIÊN QUAN:\n");
//                context.append(dataContext);
//                context.append("\n");
//            }
//        }
//
//        // Append câu hỏi gốc cuối cùng
//        context.append("Người dùng hỏi: ").append(userContent);
//
//        return context.toString();
//    }
//}
