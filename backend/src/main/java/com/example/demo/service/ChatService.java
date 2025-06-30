package com.example.demo.service;

import com.example.demo.DTO.request.ChatRequest;
import com.example.demo.DTO.response.ChatResponse;
import com.example.demo.model.Movie;
import com.example.demo.model.MovieType;
import com.example.demo.repository.MovieTypeRepository;
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
import java.util.List;
import java.util.function.Consumer;

@Service
public class ChatService {
    @Autowired
    private MovieTypeRepository movieTypeRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = "http://localhost:1234/v1/chat/completions";
    // private final String API_KEY = "lm-studio";

    public String chat(String userContent) {
        QuestionAnalyzer.QueryInfo info = QuestionAnalyzer.analyze(userContent);
        StringBuilder context = new StringBuilder();

        // Kiểm tra nếu là chào hỏi chung hoặc không có intent cụ thể
        boolean hasSpecificIntent = info.genre != null || info.askPromotion || info.askDirector ||
                info.askActor || info.askDuration || info.askPrice || info.askShowTimes;

        if (!hasSpecificIntent) {
            // Chào hỏi chung, không cần context phim
            context.append("Bạn là trợ lý AI MovieTheater thân thiện. " +
                    "Chào hỏi tự nhiên và hỏi người dùng cần hỗ trợ gì về phim, đặt vé. " +
                    "Trả lời ngắn gọn, tự nhiên.\n");
        } else {
            // Có intent cụ thể về phim
            context.append("Bạn là trợ lý AI MovieTheater.\n" +
                    "Chỉ trả lời dựa trên dữ liệu bên dưới. " +
                    "Trả lời ngắn gọn, tự nhiên, chỉ liệt kê tên phim nếu có. " +
                    "Nếu không có phim nào phù hợp, chỉ trả lời: 'Hiện tại chưa có phim " +
                    (info.genre != null ? info.genre : "phù hợp") + " nào.'\n");

            // Thêm thông tin về thể loại phim nếu có
            if (info.genre != null) {
                List<MovieType> movieTypes = movieTypeRepository
                        .findByType_NameIgnoreCaseAndMovie_IsDeletedFalse(info.genre);
                if (!movieTypes.isEmpty()) {
                    context.append("Danh sách phim thể loại ").append(info.genre).append(":\n");
                    int i = 1;
                    for (MovieType mt : movieTypes) {
                        Movie m = mt.getMovie();
                        context.append(i++).append(". ").append(m.getNameVN()).append(" (").append(m.getNameEN())
                                .append(")\n");
                    }
                } else {
                    context.append("Không có phim nào thể loại ").append(info.genre).append(".\n");
                }
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
            boolean hasSpecificIntent = info.genre != null || info.askPromotion || info.askDirector ||
                    info.askActor || info.askDuration || info.askPrice || info.askShowTimes;

            if (!hasSpecificIntent) {
                // Chào hỏi chung, không cần context phim
                context.append("Bạn là trợ lý AI MovieTheater thân thiện. " +
                        "Chào hỏi tự nhiên và hỏi người dùng cần hỗ trợ gì về phim, đặt vé. " +
                        "Trả lời ngắn gọn, tự nhiên.\n");
            } else {
                // Có intent cụ thể về phim
                context.append("Bạn là trợ lý AI MovieTheater.\n" +
                        "Chỉ trả lời dựa trên dữ liệu bên dưới. " +
                        "Trả lời ngắn gọn, tự nhiên, chỉ liệt kê tên phim nếu có. " +
                        "Nếu không có phim nào phù hợp, chỉ trả lời: 'Hiện tại chưa có phim " +
                        (info.genre != null ? info.genre : "phù hợp") + " nào.'\n");

                if (info.genre != null) {
                    List<MovieType> movieTypes = movieTypeRepository
                            .findByType_NameIgnoreCaseAndMovie_IsDeletedFalse(info.genre);
                    if (!movieTypes.isEmpty()) {
                        context.append("Danh sách phim thể loại ").append(info.genre).append(":\n");
                        int i = 1;
                        for (MovieType mt : movieTypes) {
                            Movie m = mt.getMovie();
                            context.append(i++).append(". ").append(m.getNameVN()).append(" (").append(m.getNameEN())
                                    .append(")\n");
                        }
                    } else {
                        context.append("Không có phim nào thể loại ").append(info.genre).append(".\n");
                    }
                }
            }
            String prompt = context + "Người dùng hỏi: " + userContent;

            // Build request body đúng chuẩn OpenAI API
            ObjectMapper mapper = new ObjectMapper();
            java.util.Map<String, Object> bodyMap = new java.util.HashMap<>();
            bodyMap.put("model", "vistral-7b-chat");
            java.util.List<java.util.Map<String, String>> messages = new java.util.ArrayList<>();
            java.util.Map<String, String> sysMsg = new java.util.HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", prompt);
            messages.add(sysMsg);
            java.util.Map<String, String> userMsg = new java.util.HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userContent);
            messages.add(userMsg);
            bodyMap.put("messages", messages);
            bodyMap.put("stream", true);
            String body = mapper.writeValueAsString(bodyMap);
            System.out.println("[DEBUG] LM Studio request body: " + body);
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
                System.out.println("[DEBUG] LM Studio chunk: " + chunk);
                chunkConsumer.accept(chunk);
            }
            is.close();
            conn.disconnect();
        } catch (Exception e) {
            chunkConsumer.accept("[ERROR]" + e.getMessage());
        }
    }

}
