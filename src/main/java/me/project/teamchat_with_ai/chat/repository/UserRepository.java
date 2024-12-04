package me.project.teamchat_with_ai.chat.repository;

import me.project.teamchat_with_ai.chat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIpAddress(String ipAddress);
}