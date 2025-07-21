package com.example.demo.context;

import com.example.demo.utils.QuestionAnalyzer.QueryInfo;
import java.time.LocalDateTime;

public class ChatSessionContext {
    private QueryInfo lastQueryInfo;
    private LocalDateTime lastActiveTime;

    public ChatSessionContext(QueryInfo info) {
        this.lastQueryInfo = info;
        this.lastActiveTime = LocalDateTime.now();
    }

    public QueryInfo getLastQueryInfo() {
        return lastQueryInfo;
    }

    public void setLastQueryInfo(QueryInfo lastQueryInfo) {
        this.lastQueryInfo = lastQueryInfo;
        this.lastActiveTime = LocalDateTime.now();
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    public void updateActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
    }
}
