package com.dianping.ai.service;

public class DashScopeResponse {
    private boolean success;
    private String text;
    private String message;

    public DashScopeResponse() {
    }

    public DashScopeResponse(boolean success, String text, String message) {
        this.success = success;
        this.text = text;
        this.message = message;
    }

    public static DashScopeResponse success(String text) {
        return new DashScopeResponse(true, text, null);
    }

    public static DashScopeResponse failure(String message) {
        return new DashScopeResponse(false, null, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}