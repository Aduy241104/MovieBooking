//package com.example.demo.service;
//
//import com.example.demo.context.ChatSessionContext;
//import com.example.demo.utils.QuestionAnalyzer.QueryInfo;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.concurrent.*;
//
//@Service
//public class ChatSessionContextService {
//    private final Map<String, ChatSessionContext> contextMap = new ConcurrentHashMap<>();
//    private final long TIMEOUT_MINUTES = 10; // timeout 10 phút
//
//    public ChatSessionContextService() {
//        // Tự động xóa context hết hạn
//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::cleanup, 5, 5, TimeUnit.MINUTES);
//    }
//
//    public QueryInfo getContext(String sessionId) {
//        ChatSessionContext ctx = contextMap.get(sessionId);
//        if (ctx == null)
//            return null;
//        if (Duration.between(ctx.getLastActiveTime(), LocalDateTime.now()).toMinutes() > TIMEOUT_MINUTES) {
//            contextMap.remove(sessionId);
//            return null;
//        }
//        ctx.updateActiveTime();
//        return ctx.getLastQueryInfo();
//    }
//
//    public void updateContext(String sessionId, QueryInfo info) {
//        contextMap.put(sessionId, new ChatSessionContext(info));
//    }
//
//    public void clearContext(String sessionId) {
//        contextMap.remove(sessionId);
//    }
//
//    private void cleanup() {
//        LocalDateTime now = LocalDateTime.now();
//        contextMap.entrySet().removeIf(
//                entry -> Duration.between(entry.getValue().getLastActiveTime(), now).toMinutes() > TIMEOUT_MINUTES);
//    }
//}
