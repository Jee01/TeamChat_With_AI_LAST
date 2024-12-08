package me.project.teamchat_with_ai.chat.entity;

import java.time.LocalDateTime;

public class MessageOutputDTO {
    private Long messageId;
    private String content;
    private String userName; // 작성자 닉네임 추가
    private String roomName; // 채팅방 이름
    private String timeAt; // ISO 8601 형식

    public MessageOutputDTO(Long messageId, String content, String userName, String roomName, LocalDateTime timeAt) {
        this.messageId = messageId;
        this.content = content;
        this.userName = userName;
        this.roomName = roomName;
        this.timeAt = timeAt.toString();
    }

    public MessageOutputDTO() {

    }

    // Getters and Setters
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getTimeAt() {
        return timeAt;
    }

    public void setTimeAt(String timeAt) {
        this.timeAt = timeAt;
    }
}
