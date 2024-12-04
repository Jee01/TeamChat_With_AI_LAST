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

    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @PostMapping
    public Room createRoom(@RequestBody Room room) {
        return roomRepository.save(room); //방 생성
    }


    @PutMapping("/{id}")
    public Room updateRoom(@RequestBody Room room, @PathVariable Long id) {
        Room exisitingRoom = roomRepository.findById(id).orElseThrow(()->new RuntimeException("Room not found"));
        exisitingRoom.setName(room.getName());
        return roomRepository.save(exisitingRoom);
    }
}
