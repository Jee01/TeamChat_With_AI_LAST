package me.project.teamchat_with_ai.chat.controller;

import me.project.teamchat_with_ai.chat.entity.Message;
import me.project.teamchat_with_ai.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageRepository messageRepository;

    // 특정 채팅방(roomId)의 모든 메시지 조회
    @GetMapping
    public List<Message> getMessagesByRoom(@RequestParam Long roomId) {
        return messageRepository.findByRoom_RoomIdOrderByTimeAt(roomId);
    }

    // 메시지 저장
    @PostMapping
    public Message saveMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }
}
