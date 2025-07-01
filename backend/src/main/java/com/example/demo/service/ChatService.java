package com.example.demo.service;

import com.example.demo.DTO.request.ChatRequest;
import com.example.demo.DTO.response.ChatResponse;
import com.example.demo.utils.QuestionAnalyzer;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class ChatService {
    @Autowired
    private ChatbotDataService chatbotDataService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = "http://localhost:1234/v1/chat/completions";
    // private final String API_KEY = "lm-studio";

    public String chat(String userContent) {
        QuestionAnalyzer.QueryInfo info = QuestionAnalyzer.analyze(userContent);
        StringBuilder context = new StringBuilder();

        // Kiểm tra nếu là chào hỏi chung hoặc không có intent cụ thể
        boolean hasSpecificIntent = info.hasAnyIntent();

        if (!hasSpecificIntent) {
            // Chào hỏi chung, không cần context phim
            context.append(
                    "Bạn là trợ lý AI tiếng Việt thân thiện của hệ thống MovieTheater (rạp chiếu phim, đặt vé xem phim...).\n")
                    .append("Hãy trả lời tự nhiên, thân thiện, có chủ-vị rõ ràng, đúng ngữ cảnh hội thoại.\n")
                    .append("Nếu người dùng cần hỗ trợ, hãy hỏi lại để làm rõ nhu cầu.\n");
        } else {
            // Có intent cụ thể về phim hoặc dịch vụ
            context.append(
                    "Bạn là trợ lý AI tiếng Việt thân thiện của hệ thống MovieTheater (rạp chiếu phim, đặt vé xem phim...).\n")
                    .append("QUAN TRỌNG: Chỉ trả lời dựa trên dữ liệu thực tế bên dưới, KHÔNG được sử dụng kiến thức chung.\n")
                    .append("Nếu có thông tin phim từ database, trả lời chính xác theo dữ liệu đó.\n")
                    .append("Nếu có lịch chiếu cụ thể, hãy trả lời thân thiện, có chủ-vị, theo mẫu: 'Phim ... sẽ được chiếu vào các suất sau: ...', hoặc 'Lịch chiếu phim ...: ...'. Có thể gợi ý khách kiểm tra thêm suất khác hoặc đặt vé.\n")
                    .append("Nếu có danh sách phim đang chiếu, HÃY LIỆT KÊ ĐẦY ĐỦ TẤT CẢ các phim, KHÔNG ĐƯỢC bỏ sót phim nào. KHÔNG ĐƯỢC tự ý thêm, bớt, hoặc sáng tạo tên phim ngoài danh sách context. KHÔNG ĐƯỢC trả lời các phim không có trong context.\n")
                    .append("Nếu không có dữ liệu phù hợp trong database, trả lời: 'Hiện tại chưa có thông tin/lịch chiếu cho phim này trong hệ thống.'\n")
                    .append("Trả lời ngắn gọn, tự nhiên, có chủ-vị rõ ràng, đúng ngữ cảnh hội thoại.\n");

            // Sử dụng ChatbotDataService để xây dựng context cho các intent khác
            String additionalContext = chatbotDataService.buildContextForIntent(info);
            if (!additionalContext.isEmpty()) {
                context.append("\n").append(additionalContext);
            } else if (info.movieName != null) {
                // Nếu hỏi về phim cụ thể nhưng không có dữ liệu trong database
                context.append("\nKhông tìm thấy thông tin phim '").append(info.movieName)
                        .append("' trong hệ thống.\n");
            }
        }

        String prompt = context + "Người dùng hỏi: " + userContent;

        ChatRequest request = new ChatRequest();
        request.setModel("vistral-7b-chat");

        ChatRequest.Message systemMsg = new ChatRequest.Message();
        systemMsg.setRole("system");
        systemMsg.setContent(prompt);

        ChatRequest.Message userMsg = new ChatRequest.Message();
        userMsg.setRole("user");
        userMsg.setContent(userContent);

        request.setMessages(List.of(systemMsg, userMsg));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // headers.setBearerAuth(API_KEY);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ChatResponse> response = restTemplate.postForEntity(API_URL, entity, ChatResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().getChoices().get(0).getMessage().getContent();
        }
        return "Xin lỗi, tôi không thể trả lời lúc này.";
    }

    public void chatStream(ChatRequest request, Consumer<String> chunkConsumer) {
        try {
            String userContent = null;
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                for (ChatRequest.Message msg : request.getMessages()) {
                    if ("user".equalsIgnoreCase(msg.getRole())) {
                        userContent = msg.getContent();
                        break;
                    }
                }
            }
            if (userContent == null)
                userContent = "";
            QuestionAnalyzer.QueryInfo info = QuestionAnalyzer.analyze(userContent);
            StringBuilder context = new StringBuilder();

            // Kiểm tra nếu là chào hỏi chung hoặc không có intent cụ thể
            boolean hasSpecificIntent = info.hasAnyIntent();

            if (!hasSpecificIntent) {
                // Chào hỏi chung, không cần context phim
                context.append(
                        "Bạn là trợ lý AI tiếng Việt thân thiện của hệ thống MovieTheater (rạp chiếu phim, đặt vé xem phim...).\n")
                        .append("Hãy trả lời tự nhiên, thân thiện, có chủ-vị rõ ràng, đúng ngữ cảnh hội thoại.\n")
                        .append("Nếu người dùng cần hỗ trợ, hãy hỏi lại để làm rõ nhu cầu.\n");
            } else {
                // Có intent cụ thể về phim hoặc dịch vụ
                context.append(
                        "Bạn là trợ lý AI tiếng Việt thân thiện của hệ thống MovieTheater (rạp chiếu phim, đặt vé xem phim...).\n")
                        .append("QUAN TRỌNG: Chỉ trả lời dựa trên dữ liệu thực tế bên dưới, KHÔNG được sử dụng kiến thức chung.\n")
                        .append("Nếu có thông tin phim từ database, trả lời chính xác theo dữ liệu đó.\n")
                        .append("Nếu có lịch chiếu cụ thể, hãy trả lời thân thiện, có chủ-vị, theo mẫu: 'Phim ... sẽ được chiếu vào các suất sau: ...', hoặc 'Lịch chiếu phim ...: ...'. Có thể gợi ý khách kiểm tra thêm suất khác hoặc đặt vé.\n")
                        .append("Nếu có danh sách phim đang chiếu, HÃY LIỆT KÊ ĐẦY ĐỦ TẤT CẢ các phim, KHÔNG ĐƯỢC bỏ sót phim nào. KHÔNG ĐƯỢC tự ý thêm, bớt, hoặc sáng tạo tên phim ngoài danh sách context. KHÔNG ĐƯỢC trả lời các phim không có trong context.\n")
                        .append("Nếu không có dữ liệu phù hợp trong database, trả lời: 'Hiện tại chưa có thông tin/lịch chiếu cho phim này trong hệ thống.'\n")
                        .append("Trả lời ngắn gọn, tự nhiên, có chủ-vị rõ ràng, đúng ngữ cảnh hội thoại.\n");

                // Sử dụng ChatbotDataService để xây dựng context cho các intent khác
                String additionalContext = chatbotDataService.buildContextForIntent(info);
                if (!additionalContext.isEmpty()) {
                    context.append("\n").append(additionalContext);
                } else if (info.movieName != null) {
                    // Nếu hỏi về phim cụ thể nhưng không có dữ liệu trong database
                    context.append("\nKhông tìm thấy thông tin phim '").append(info.movieName)
                            .append("' trong hệ thống.\n");
                }
            }
            String prompt = context + "Người dùng hỏi: " + userContent;

            // Build request body đúng chuẩn OpenAI API
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("model", "vistral-7b-chat");

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", prompt);
            messages.add(sysMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userContent);
            messages.add(userMsg);
            bodyMap.put("messages", messages);
            bodyMap.put("stream", true);

            String body = mapper.writeValueAsString(bodyMap);
            // System.out.println("[DEBUG] LM Studio request body: " + body);
            URI uri = URI.create(API_URL);
            URL url = uri.toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.getBytes());

            InputStream is = conn.getInputStream();
            byte[] buffer = new byte[2048];
            int len;
            while ((len = is.read(buffer)) != -1) {
                String chunk = new String(buffer, 0, len);
                // System.out.println("[DEBUG] LM Studio chunk: " + chunk);
                chunkConsumer.accept(chunk);
            }
            is.close();
            conn.disconnect();
        } catch (Exception e) {
            chunkConsumer.accept("[ERROR]" + e.getMessage());
        }
    }

}
