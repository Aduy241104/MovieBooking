package com.example.demo.controller;

import com.example.demo.DTO.request.ChatRequest;
import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.ChatResponse;
import com.example.demo.service.ChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping("/api/public/chatbot")
public class ChatbotController {
    @Autowired
    private ChatService chatService;

    @PostMapping
    public Object chat(@RequestBody ChatRequest request,
            @RequestParam(value = "stream", required = false) Boolean stream, HttpServletResponse response) {
        System.out.println("Received chatbot request: " + request);
        boolean isStream = (request.getStream() != null && request.getStream()) || (stream != null && stream);
        if (isStream) {
            response.setContentType("application/octet-stream");
            return (StreamingResponseBody) outputStream -> {
                chatService.chatStream(request, chunk -> {
                    try {
                        outputStream.write(chunk.getBytes());
                        outputStream.flush();
                    } catch (Exception e) {
                        // handle error
                    }
                });
            };
        } else {
            // Lấy content của message user đầu tiên trong mảng messages
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
            String answer = chatService.chat(userContent);
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setChoices(List.of(new ChatResponse.Choice() {
                {
                    setMessage(new ChatResponse.Message() {
                        {
                            setRole("assistant");
                            setContent(answer);
                        }
                    });
                }
            }));
            return ApiResponse.<ChatResponse>builder()
                    .status(200)
                    .message("Call LM Studio API successfully")
                    .result(chatResponse)
                    .build();
        }
    }
}
