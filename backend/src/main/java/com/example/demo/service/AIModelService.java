//package com.example.demo.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.stereotype.Service;
//
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.URI;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Consumer;
//
///**
// * Service chuyên dụng để gọi AI model
// * Tách biệt logic gọi API với logic xây dựng context
// */
//@Service
//public class AIModelService {
//
//    private final String API_URL = "http://localhost:11434/api/chat";
////    private final String API_KEY = "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";
//
//    /**
//     * Gọi AI model với streaming response
//     *
//     * @param systemPrompt  System prompt đã được tối ưu
//     * @param userContent   Câu hỏi gốc của user
//     * @param chunkConsumer Consumer để xử lý từng chunk response
//     */
//    public void callAIStream(String systemPrompt, String userContent, Consumer<String> chunkConsumer) {
//        try {
//            // Build request body theo chuẩn OpenAI API
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, Object> bodyMap = new HashMap<>();
//            bodyMap.put("model", "gemma3:4b");
//
//            List<Map<String, String>> messages = new ArrayList<>();
//
//            // System message
//            Map<String, String> sysMsg = new HashMap<>();
//            sysMsg.put("role", "system");
//            sysMsg.put("content", systemPrompt);
//            messages.add(sysMsg);
//
//            // User message
//            Map<String, String> userMsg = new HashMap<>();
//            userMsg.put("role", "user");
//            userMsg.put("content", userContent);
//            messages.add(userMsg);
//
//            bodyMap.put("messages", messages);
//            bodyMap.put("stream", true);
//
//            String body = mapper.writeValueAsString(bodyMap);
//
//            // Tạo HTTP connection
//            URI uri = URI.create(API_URL);
//            URL url = uri.toURL();
//
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
////            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
//            conn.setDoOutput(true);
//
//            // Gửi request
//            conn.getOutputStream().write(body.getBytes());
//
//            // Đọc streaming response
//            InputStream is = conn.getInputStream();
//            byte[] buffer = new byte[4096];
//            int len;
//            while ((len = is.read(buffer)) != -1) {
//                String chunk = new String(buffer, 0, len);
//                chunkConsumer.accept(chunk);
//            }
//
//            is.close();
//            conn.disconnect();
//
//        } catch (Exception e) {
//            System.err.println("Error calling AI model: " + e.getMessage());
//            chunkConsumer.accept("Xin lỗi, tôi không thể trả lời lúc này do lỗi kỹ thuật.");
//        }
//    }
//}
