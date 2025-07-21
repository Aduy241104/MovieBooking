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
            // Merge context: chỉ merge khi thật sự phù hợp và liên quan
            if (oldInfo != null && shouldMergeContext(userContent, newInfo, oldInfo)) {
                // Chỉ merge movieName khi:
                // 1. Câu hỏi mới không có tên phim (newInfo.movieName == null)
                // 2. Câu hỏi mới liên quan đến phim (có intent về phim)
                // 3. Câu hỏi mới KHÔNG phải là câu hỏi về tên phim cụ thể mới
                // 4. Câu hỏi mới KHÔNG phải là câu hỏi chung chung về phim (như "phim gì",
                // "phim nào")
                if (newInfo.movieName == null &&
                        (newInfo.movie_showtimes || newInfo.movie_info || newInfo.movie_pricing ||
                                newInfo.movie_booking || newInfo.movie_release_date)
                        &&
                        !userContent.toLowerCase().matches(
                                ".*phim\\s+[a-zA-Z0-9\u00C0-\u1EF9\\s:&'\"!.,-]+\\s+(khi nào chiếu|có|được|là).*")
                        &&
                        !userContent.toLowerCase().matches(".*(phim gì|phim nào|những bộ phim|các bộ phim).*")) {
                    newInfo.movieName = oldInfo.movieName;
                }

                // Không merge genre trừ khi câu hỏi thực sự liên quan đến thể loại
                if (newInfo.genre == null && newInfo.movie_genre && oldInfo.genre != null) {
                    newInfo.genre = oldInfo.genre;
                }

                // Chỉ merge date/time khi câu hỏi về lịch chiếu
                if (newInfo.date == null && newInfo.movie_showtimes && oldInfo.date != null) {
                    newInfo.date = oldInfo.date;
                }
                if (newInfo.time == null && newInfo.movie_showtimes && oldInfo.time != null) {
                    newInfo.time = oldInfo.time;
                }
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
     * Kiểm tra xem có nên merge context hay không
     */
    private boolean shouldMergeContext(String userContent,
            com.example.demo.utils.QuestionAnalyzer.QueryInfo newInfo,
            com.example.demo.utils.QuestionAnalyzer.QueryInfo oldInfo) {
        String lowerContent = userContent.toLowerCase();

        // Không merge nếu câu hỏi mới có intent hoàn toàn khác biệt
        if (newInfo.account_management || newInfo.contact_support ||
                newInfo.cinema_location || newInfo.promotion_discount ||
                newInfo.technical_support || newInfo.ticket_policy) {
            return false;
        }

        // Không merge nếu câu hỏi hỏi về thể loại mới
        if (lowerContent.contains("thể loại") || lowerContent.contains("genre")) {
            return false;
        }

        // Không merge nếu câu hỏi hỏi về danh sách phim chung
        if (lowerContent.matches(".*(phim gì|phim nào|những bộ phim|các bộ phim|danh sách phim).*")) {
            return false;
        }

        return true;
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
