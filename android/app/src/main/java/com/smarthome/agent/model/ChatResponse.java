package com.smarthome.agent.model;

public class ChatResponse {
    private String message;
    private boolean requiresConfirmation;
    private boolean success;

    public ChatResponse(String message, boolean requiresConfirmation, boolean success) {
        this.message = message;
        this.requiresConfirmation = requiresConfirmation;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
