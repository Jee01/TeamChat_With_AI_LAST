package me.project.teamchat_with_ai.chat.controller;

import me.project.teamchat_with_ai.chat.entity.User;
import me.project.teamchat_with_ai.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 유저 정보 조회 (IP 기반)
    @GetMapping("/by-ip")
    public User getUserByIp(@RequestParam String ipAddress) {
        return userRepository.findByIpAddress(ipAddress).orElse(null);
    }

    // 유저 저장 및 업데이트
    @PostMapping
    public User saveUser(@RequestBody User user) {
        return userRepository.save(user);
    }
}
