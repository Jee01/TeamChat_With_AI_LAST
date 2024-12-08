package me.project.teamchat_with_ai.chat.controller;

import jakarta.servlet.http.HttpServletRequest;
import me.project.teamchat_with_ai.chat.entity.User;
import me.project.teamchat_with_ai.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 특정 IP로 사용자 조회
    @GetMapping("/{ipAddress}")
    public User getUserByIpAddress(@PathVariable String ipAddress) {
        return userRepository.findByIpAddress(ipAddress)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    // 새 사용자 생성 (IP 기반)
    @PostMapping("/ip")
    public User getOrCreateUserByIp(HttpServletRequest request) {
        String ip = getUserIp(request);
        return userRepository.findByIpAddress(ip)
                .orElseGet(() -> userRepository.save(new User(ip, "기본닉네임"))); // 기본 닉네임 설정
    }

    // 닉네임 업데이트
    @PutMapping("/{userId}/nickname")
    public User updateUserNickname(@PathVariable Long userId, @RequestBody String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    // 사용자 IP 가져오기
    @GetMapping("/api/user-ip")
    public String getUserIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1"; // 로컬호스트 IP 정규화
        }
        return ip;
    }


}
