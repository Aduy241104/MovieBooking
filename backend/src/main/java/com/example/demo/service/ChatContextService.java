package com.example.demo.service;

import com.example.demo.utils.QuestionAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service chuyên dụng để xây dựng context tối ưu cho chatbot
 * Chỉ lấy dữ liệu thực sự cần thiết dựa trên intent của user
 */
@Service
public class ChatContextService {

    @Autowired
    private OptimizedChatbotDataService optimizedChatbotDataService;

    /**
     * Xây dựng system prompt và context dành cho AI model
     *
     * @param userContent Câu hỏi gốc của user
     * @return Context đầy đủ để gửi cho AI model
     */
    public String buildOptimizedContext(String userContent) {
        QuestionAnalyzer.QueryInfo info = QuestionAnalyzer.analyze(userContent);
        StringBuilder context = new StringBuilder();

        // Base system prompt
        context.append(
                "Bạn là nhân viên tư vấn AI của rạp MovieTheater (chỉ có 1 chi nhánh duy nhất, không có nhiều rạp, không hỏi lại về địa điểm).\n");

        // Kiểm tra nếu có intent cụ thể
        boolean hasSpecificIntent = info.hasAnyIntent();

        if (!hasSpecificIntent) {
            // Chào hỏi chung, không cần context database
            context.append("Hãy trả lời ngắn gọn, tự nhiên, thân thiện.\n");
            context.append("Nếu người dùng cần hỗ trợ về phim, lịch chiếu, đặt vé, hãy hỏi lại để làm rõ nhu cầu.\n");
        } else {
            // Có intent cụ thể - thêm quy tắc và context data
            context.append("QUY TẮC QUAN TRỌNG:\n");
            context.append("- Chỉ trả lời dựa trên dữ liệu thực tế bên dưới, KHÔNG sử dụng kiến thức chung\n");
            context.append("- Sau khi đặt vé thành công, khách hàng KHÔNG thể đổi suất chiếu hoặc hủy/hoàn vé\n");
            context.append(
                    "- Nếu không có dữ liệu phù hợp, trả lời: 'Hiện tại chưa có thông tin phù hợp trong hệ thống'\n");
            context.append("- Trả lời ngắn gọn, tự nhiên, phù hợp với ngữ cảnh\n\n");

            // Lấy context data tối ưu dựa trên intent
            System.out.println(">>> Analyzing user intent: " + info);
            String dataContext = optimizedChatbotDataService.buildContextForIntent(info);
            System.out.println(">>> Context data for user intent: " + dataContext);
            if (dataContext.trim().startsWith("Xin lỗi, hiện tại hệ thống chưa có thông tin về phim này")) {
                return dataContext.trim(); // Trả lời trực tiếp không cần gọi AI
            }

            if (!dataContext.isEmpty()) {
                context.append("DỮ LIỆU LIÊN QUAN:\n");
                context.append(dataContext);
                context.append("\n");
            }
        }

        // Append câu hỏi gốc cuối cùng
        context.append("Người dùng hỏi: ").append(userContent);

        return context.toString();
    }

    /**
     * Xây dựng system prompt và context dành cho AI model (Overloaded method)
     *
     * @param userContent Câu hỏi gốc của user
     * @param info        Thông tin truy vấn đã phân tích
     * @return Context đầy đủ để gửi cho AI model
     */
    public String buildOptimizedContext(String userContent, QuestionAnalyzer.QueryInfo info) {
        StringBuilder context = new StringBuilder();

        // Base system prompt
        context.append(
                "Bạn là nhân viên tư vấn AI của rạp MovieTheater (chỉ có 1 chi nhánh duy nhất, không có nhiều rạp, không hỏi lại về địa điểm).\n");

        // Kiểm tra nếu có intent cụ thể
        boolean hasSpecificIntent = info.hasAnyIntent();

        if (!hasSpecificIntent) {
            // Chào hỏi chung, không cần context database
            context.append("Hãy trả lời ngắn gọn, tự nhiên, thân thiện.\n");
            context.append("Nếu người dùng cần hỗ trợ về phim, lịch chiếu, đặt vé, hãy hỏi lại để làm rõ nhu cầu.\n");
        } else {
            // Có intent cụ thể - thêm quy tắc và context data
            context.append("QUY TẮC QUAN TRỌNG:\n");
            context.append("- Chỉ trả lời dựa trên dữ liệu thực tế bên dưới\n");
            context.append("- Sau khi đặt vé thành công, khách hàng KHÔNG thể đổi suất chiếu hoặc hủy/hoàn vé\n");
            context.append(
                    "- Nếu không có dữ liệu phù hợp, trả lời: 'Hiện tại chưa có thông tin phù hợp trong hệ thống'\n");
            context.append("- Trả lời ngắn gọn, tự nhiên, phù hợp với ngữ cảnh\n\n");

            // Lấy context data tối ưu dựa trên intent
            System.out.println(">>> Analyzing user intent: " + info);
            String dataContext = optimizedChatbotDataService.buildContextForIntent(info);
            System.out.println(">>> Context data for user intent: " + dataContext);
            if (dataContext.trim().startsWith("Xin lỗi, hiện tại hệ thống chưa có thông tin về phim này")) {
                return dataContext.trim(); // Trả lời trực tiếp không cần gọi AI
            }

            if (!dataContext.isEmpty()) {
                context.append("DỮ LIỆU LIÊN QUAN:\n");
                context.append(dataContext);
                context.append("\n");
            }
        }

        // Append câu hỏi gốc cuối cùng
        context.append("Người dùng hỏi: ").append(userContent);

        return context.toString();
    }
}
