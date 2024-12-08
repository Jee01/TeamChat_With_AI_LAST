package me.project.teamchat_with_ai.chat.repository;

import me.project.teamchat_with_ai.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // 특정 채팅방(room_id)의 메시지를 시간순으로 가져오기
    @Query("SELECT m FROM Message m JOIN FETCH m.room JOIN FETCH m.user WHERE m.room.roomId = :roomId ORDER BY m.timeAt")
    List<Message> findByRoom_RoomIdOrderByTimeAt(@Param("roomId") Long roomId);

}