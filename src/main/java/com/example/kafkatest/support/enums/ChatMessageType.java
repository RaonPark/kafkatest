package com.example.kafkatest.support.enums;

public enum ChatMessageType {
    TEXT("T"),
    IMAGE("I"),
    EMOTICON("E");

    private final String messageType;

    ChatMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageType() {
        return messageType;
    }
}
