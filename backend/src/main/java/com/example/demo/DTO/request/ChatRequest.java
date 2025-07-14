package com.example.demo.DTO.request;

import java.util.List;

import lombok.Data;

@Data
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private Boolean stream; // Thêm trường này để nhận stream từ frontend
    private String sessionId; // Thêm trường sessionId để quản lý phiên chat

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
