package com.example.demo.controller;

import com.example.demo.DTO.request.ChatRequest;
import com.example.demo.service.ChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Controller xử lý chatbot requests
 * Chỉ hỗ trợ streaming response để tối ưu user experience
 */
@RestController
@RequestMapping("/api/public/chatbot")
public class ChatbotController {

    @Autowired
    private ChatService chatService;

    /**
     * Endpoint duy nhất cho chatbot - chỉ hỗ trợ streaming
     * 
     * @param request  Chat request từ client
     * @param response HTTP response
     * @return StreamingResponseBody
     */
    @PostMapping
    public StreamingResponseBody chat(@RequestBody ChatRequest request, HttpServletResponse response) {
        System.out.println(">>> Received chatbot request: " + request);

        // Set content type cho streaming response
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");

        return outputStream -> {
            chatService.chatStream(request, chunk -> {
                try {
                    outputStream.write(chunk.getBytes("UTF-8"));
                    outputStream.flush();
                } catch (Exception e) {
                    System.err.println("Error writing chunk: " + e.getMessage());
                }
            });
        };
    }
}
