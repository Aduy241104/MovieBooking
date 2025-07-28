package com.example.demo.DTO.response;

public class ResAccountAPI {
    private boolean result;
    private String message;

    public ResAccountAPI() {
    }

    public ResAccountAPI(boolean result, String message) {
        this.result = result;
        this.message = message;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
