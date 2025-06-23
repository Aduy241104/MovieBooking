package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiChatService {
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public String askGemini(String question) {
        RestTemplate restTemplate = new RestTemplate();

        // Build request body
        Map<String, Object> part = Map.of("text", question);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> body = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String url = geminiApiUrl + "?key=" + geminiApiKey;

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> respBody = response.getBody();
                if (respBody != null && respBody.containsKey("candidates")) {
                    List candidates = (List) respBody.get("candidates");
                    if (!candidates.isEmpty()) {
                        Map candidate = (Map) candidates.get(0);
                        Map contentMap = (Map) candidate.get("content");
                        List parts = (List) contentMap.get("parts");
                        if (!parts.isEmpty()) {
                            Map partMap = (Map) parts.get(0);
                            return (String) partMap.get("text");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi kết nối tới Gemini: " + e.getMessage();
        }
        return "Xin lỗi, tôi không thể trả lời câu hỏi này lúc này.";
    }
}
