package me.project.teamchat_with_ai.chat.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Table(name = "message")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Lazy Proxy 무시
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값 제외
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "time_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime timeAt;

    // 기본 생성자
    public Message() {}

    // 모든 필드를 사용하는 생성자
    public Message(Room room, User user, String content) {
        this.room = room;
        this.user = user;
        this.content = content;
        this.timeAt = LocalDateTime.now(); // 현재 시간 설정
    }

    // Getters and Setters

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimeAt() {
        return timeAt;
    }

    public void setTimeAt(LocalDateTime timeAt) {
        this.timeAt = timeAt;
    }

}
