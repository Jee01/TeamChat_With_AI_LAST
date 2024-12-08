package me.project.teamchat_with_ai.chat.entity;

import java.time.LocalDateTime;

public class MessageDTO {
    private Long messageId;
    private Long roomId;
    private Long userId;
    private String content;
    private LocalDateTime timeAt;

    public MessageDTO(Message message) {
        this.messageId = message.getMessageId();
        this.roomId = message.getRoom().getRoomId();
        this.userId = message.getUser().getUserId();
        this.content = message.getContent();
        this.timeAt = message.getTimeAt();
    }

    // Getters
    public Long getMessageId() {
        return messageId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimeAt() {
        return timeAt;
    }
}
