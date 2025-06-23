package com.example.demo.controller;

import com.example.demo.service.GeminiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/gemini-chat")
public class GeminiChatController {
    @Autowired
    private GeminiChatService geminiChatService;

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        String answer = geminiChatService.askGemini(question);
        return Map.of("answer", answer);
    }
}
