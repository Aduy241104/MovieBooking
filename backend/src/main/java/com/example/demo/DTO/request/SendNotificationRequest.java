package com.example.demo.DTO.request;

import java.util.List;

public class SendNotificationRequest {
    private List<Long> accountIds;
    private String title;
    private String content;
    private String type;

    public SendNotificationRequest() {
    }

    public SendNotificationRequest(List<Long> accountIds, String title, String content, String type) {
        this.accountIds = accountIds;
        this.title = title;
        this.content = content;
        this.type = type;
    }

    public List<Long> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<Long> accountIds) {
        this.accountIds = accountIds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
