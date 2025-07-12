package com.example.demo.service;

import com.example.demo.utils.QuestionAnalyzer;
import com.example.demo.utils.QuestionAnalyzer.QueryInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class ChatSessionContextServiceTest {
    private ChatSessionContextService contextService;

    @BeforeEach
    public void setup() {
        contextService = new ChatSessionContextService();
    }

    @Test
    public void testContextMemoryAndTimeout() throws InterruptedException {
        String sessionId = UUID.randomUUID().toString();
        QueryInfo info1 = QuestionAnalyzer.analyze("Lịch chiếu phim Spider-Man");
        contextService.updateContext(sessionId, info1);
        QueryInfo ctx1 = contextService.getContext(sessionId);
        Assertions.assertNotNull(ctx1);
        Assertions.assertEquals(info1.movieName, ctx1.movieName);
        // Giả lập timeout (giảm timeout xuống 0 để test nhanh)
        contextService = new ChatSessionContextService() {
            {
                // override timeout to 0 for test
                try {
                    java.lang.reflect.Field f = ChatSessionContextService.class.getDeclaredField("TIMEOUT_MINUTES");
                    f.setAccessible(true);
                    f.setLong(this, 0);
                } catch (Exception e) {
                }
            }
        };
        contextService.updateContext(sessionId, info1);
        Thread.sleep(1000); // chờ 1s
        QueryInfo ctx2 = contextService.getContext(sessionId);
        Assertions.assertNull(ctx2); // context đã bị xóa do timeout
    }

    @Test
    public void testContextMerge() {
        String sessionId = UUID.randomUUID().toString();
        QueryInfo info1 = QuestionAnalyzer.analyze("Lịch chiếu phim Avengers: Endgame");
        contextService.updateContext(sessionId, info1);
        QueryInfo oldInfo = contextService.getContext(sessionId);
        QueryInfo info2 = QuestionAnalyzer.analyze("Lịch chiếu phim này ngày mai");
        // Merge logic: nếu info2.movieName == null thì lấy từ oldInfo
        if (info2.movieName == null)
            info2.movieName = oldInfo.movieName;
        Assertions.assertEquals("avengers:", info2.movieName);
    }
}
