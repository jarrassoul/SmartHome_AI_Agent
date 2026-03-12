package com.smarthome.agent.model;

public class ConfirmRequest {
    private String action;
    private String userId;

    public ConfirmRequest(String action, String userId) {
        this.action = action;
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
