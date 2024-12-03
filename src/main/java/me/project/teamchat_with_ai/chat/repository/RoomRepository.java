package me.project.teamchat_with_ai.chat.repository;

import me.project.teamchat_with_ai.chat.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//DB와 상호작용
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Room findByName(String name);
    //기본 제공 메서드
    //save(Room room) 새 채팅방 저장
    //findById(Long id) ID로 채팅방 조회
}
