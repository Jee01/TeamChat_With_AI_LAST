package me.project.teamchat_with_ai.chat.controller;

import me.project.teamchat_with_ai.chat.entity.Room;
import me.project.teamchat_with_ai.chat.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    // 모든 채팅방 반환
    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // 특정 room_id로 채팅방 조회
    @GetMapping("/{roomId}")
    public Room getRoomById(@PathVariable Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    // 새 채팅방 생성
    @PostMapping
    public Room createRoom(@RequestBody Room room) {
        return roomRepository.save(room);
    }
}
