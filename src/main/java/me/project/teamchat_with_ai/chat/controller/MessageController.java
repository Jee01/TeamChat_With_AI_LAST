package me.project.teamchat_with_ai.chat.controller;

import me.project.teamchat_with_ai.chat.entity.Message;
import me.project.teamchat_with_ai.chat.entity.MessageDTO;
import me.project.teamchat_with_ai.chat.entity.MessageOutputDTO;
import me.project.teamchat_with_ai.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/{roomId}")
    public ResponseEntity<List<MessageDTO>> getMessagesByRoom(@PathVariable Long roomId) {
        List<Message> messages = messageRepository.findByRoom_RoomIdOrderByTimeAt(roomId);
        List<MessageDTO> messageDTOs = messages.stream()
                .map(MessageDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(messageDTOs);
    }

}

