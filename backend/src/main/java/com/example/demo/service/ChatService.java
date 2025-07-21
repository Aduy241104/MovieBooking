package com.example.demo.service;

import com.example.demo.DTO.request.ChatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * Service chính cho chatbot
 * Điều phối giữa context service và AI model service
 */
@Service
public class ChatService {

    @Autowired
    private ChatContextService chatContextService;

    @Autowired
    private AIModelService aiModelService;

    @Autowired
    private ChatSessionContextService chatSessionContextService;

    /**
     * Xử lý chat request với streaming response
     * 
     * @param request       Chat request từ client
     * @param chunkConsumer Consumer để xử lý từng chunk response
     */
    public void chatStream(ChatRequest request, Consumer<String> chunkConsumer) {
        try {
            // Lấy user content từ request
            String userContent = extractUserContent(request);
            if (userContent == null || userContent.trim().isEmpty()) {
                chunkConsumer.accept("Xin lỗi, tôi không nhận được câu hỏi của bạn.");
                return;
            }

            // Lấy sessionId (tạm thời random UUID nếu chưa có)
            String sessionId = getSessionIdFromRequest(request);

            // Lấy context cũ
            com.example.demo.utils.QuestionAnalyzer.QueryInfo oldInfo = chatSessionContextService.getContext(sessionId);
            // Phân tích câu hỏi mới
            com.example.demo.utils.QuestionAnalyzer.QueryInfo newInfo = com.example.demo.utils.QuestionAnalyzer
                    .analyze(userContent);
            // Merge context: nếu thiếu thông tin, lấy từ context cũ
            if (oldInfo != null) {
                if (newInfo.movieName == null)
                    newInfo.movieName = oldInfo.movieName;
                if (newInfo.genre == null)
                    newInfo.genre = oldInfo.genre;
                if (newInfo.date == null)
                    newInfo.date = oldInfo.date;
                if (newInfo.time == null)
                    newInfo.time = oldInfo.time;
                // ... mở rộng cho các trường khác nếu cần ...
            }
            // Lưu lại context mới
            chatSessionContextService.updateContext(sessionId, newInfo);

            // Xây dựng context tối ưu (truyền QueryInfo vào ChatContextService)
            String optimizedContext = chatContextService.buildOptimizedContext(userContent, newInfo);
            System.out.println(">>> Optimized context for user: " + optimizedContext);

            // Kiểm tra nếu có thể trả lời trực tiếp (không cần gọi AI)
            if (optimizedContext.startsWith("Xin lỗi, hiện tại hệ thống chưa có thông tin về phim này") ||
                    optimizedContext.startsWith("Hiện tại chưa có thông tin phù hợp trong hệ thống")) {
                chunkConsumer.accept(optimizedContext);
                return;
            }

            // Gọi AI model với streaming
            aiModelService.callAIStream(optimizedContext, userContent, chunkConsumer);

        } catch (Exception e) {
            System.err.println("Error in chatStream: " + e.getMessage());
            chunkConsumer.accept("Xin lỗi, đã xảy ra lỗi kỹ thuật. Vui lòng thử lại sau.");
        }
    }

    // Lấy sessionId từ request
    private String getSessionIdFromRequest(ChatRequest request) {
        // Ưu tiên lấy từ request (header, cookie, hoặc trường sessionId trong
        // ChatRequest)
        if (request.getSessionId() != null && !request.getSessionId().isEmpty()) {
            return request.getSessionId();
        }
        // Nếu không có thì mới random (chỉ nên random ở lần đầu)
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Trích xuất user content từ chat request
     * 
     * @param request Chat request
     * @return User content hoặc null nếu không tìm thấy
     */
    private String extractUserContent(ChatRequest request) {
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (ChatRequest.Message msg : request.getMessages()) {
                if ("user".equalsIgnoreCase(msg.getRole())) {
                    return msg.getContent();
                }
            }
        }
        return null;
    }
}
