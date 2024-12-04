package me.project.teamchat_with_ai.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.project.teamchat_with_ai.chat.entity.Message;
import me.project.teamchat_with_ai.chat.entity.Room;
import me.project.teamchat_with_ai.chat.entity.User;
import me.project.teamchat_with_ai.chat.repository.MessageRepository;
import me.project.teamchat_with_ai.chat.repository.RoomRepository;
import me.project.teamchat_with_ai.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SocketHandler extends TextWebSocketHandler {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<Long, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, String> params = parseQueryString(session.getUri().getQuery());
        Long roomId = Long.valueOf(params.get("room_id"));

        roomSessions.putIfAbsent(roomId, new ArrayList<>());
        roomSessions.get(roomId).add(session);
        System.out.println("Session connected: " + session.getId() + " to room: " + roomId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, String> params = parseQueryString(session.getUri().getQuery());
        Long roomId = Long.valueOf(params.get("room_id"));

        // 메시지 파싱
        Message parsedMessage = objectMapper.readValue(message.getPayload(), Message.class);

        // Room 조회 및 설정
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        parsedMessage.setRoom(room);

        // User 조회 및 설정
        Long userId = parsedMessage.getUser().getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        parsedMessage.setUser(user);

        // 메시지 저장
        parsedMessage.setTimeAt(java.time.LocalDateTime.now());
        messageRepository.save(parsedMessage);

        // 메시지 브로드캐스팅
        List<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            for (WebSocketSession s : sessions) {
                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(parsedMessage)));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        roomSessions.values().forEach(sessions -> sessions.remove(session));
        System.out.println("Session disconnected: " + session.getId());
    }

    private Map<String, String> parseQueryString(String query) {
        Map<String, String> map = new HashMap<>();
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            map.put(keyValue[0], keyValue[1]);
        }
        return map;
    }
}