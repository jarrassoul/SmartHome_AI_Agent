package com.smarthome.agent.model;

public class Message {
    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;

    private String content;
    private int type;
    private boolean requiresConfirmation;

    public Message(String content, int type) {
        this.content = content;
        this.type = type;
        this.requiresConfirmation = false;
    }

    public Message(String content, int type, boolean requiresConfirmation) {
        this.content = content;
        this.type = type;
        this.requiresConfirmation = requiresConfirmation;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }
}
