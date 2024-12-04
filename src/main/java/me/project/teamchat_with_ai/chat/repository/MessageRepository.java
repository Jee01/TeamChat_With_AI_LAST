package me.project.teamchat_with_ai.chat.repository;

import me.project.teamchat_with_ai.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRoom_RoomIdOrderByTimeAt(Long roomId);
}