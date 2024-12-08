package me.project.teamchat_with_ai.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.project.teamchat_with_ai.chat.entity.*;
import me.project.teamchat_with_ai.chat.repository.MessageRepository;
import me.project.teamchat_with_ai.chat.repository.RoomRepository;
import me.project.teamchat_with_ai.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class SocketHandler extends TextWebSocketHandler {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Map<Long, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);

    @Autowired
    public SocketHandler(MessageRepository messageRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
        // Java 8 DateTime 모듈 등록
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private MessageOutputDTO convertToDTO(Message message) {
        return new MessageOutputDTO(
                message.getMessageId(),
                message.getContent(),
                message.getUser().getNickname(), // 작성자 닉네임 추가
                message.getRoom().getName(), // 채팅방 이름
                message.getTimeAt()
        );
    }


    private String getUserIpFromSession(WebSocketSession session) {
        String ip = session.getRemoteAddress().getAddress().getHostAddress();
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1"; // 로컬호스트 IP 정규화
        }
        return ip;
    }

    @Override
    @Transactional
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long roomId = getRoomIdFromSession(session);
        String userIp = getUserIpFromSession(session); // 외부 IP 처리

        logger.info("WebSocket 요청 발생: IP = " + session.getRemoteAddress());

        logger.info("WebSocket 연결 성공. Room ID: {}, User IP: {}", roomId, userIp);

        roomSessions.putIfAbsent(roomId, new ArrayList<>());
        roomSessions.get(roomId).add(session);

        logger.info("Room ID {}의 현재 세션 수: {}", roomId, roomSessions.get(roomId).size());

        // 기존 메시지 조회
        List<Message> messages = messageRepository.findByRoom_RoomIdOrderByTimeAt(roomId);
        logger.info("기존 메시지 개수: {}", messages.size());

        List<MessageOutputDTO> messageDTOs = messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 초기 메시지 전송
        String payload = objectMapper.writeValueAsString(messageDTOs);
        session.sendMessage(new TextMessage(payload));
        logger.info("초기 메시지가 전송되었습니다.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long roomId = getRoomIdFromSession(session);

        // 클라이언트에서 전송된 JSON 데이터를 DTO로 변환
        MessageInputDTO inputDTO = objectMapper.readValue(message.getPayload(), MessageInputDTO.class);

        // Room 및 User 객체 조회
        Room room = roomRepository.findById(inputDTO.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        User user = userRepository.findById(inputDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Message 엔티티 생성
        Message newMessage = new Message();
        newMessage.setRoom(room);  // Room 설정
        newMessage.setUser(user);
        newMessage.setContent(inputDTO.getContent());
        newMessage.setTimeAt(LocalDateTime.now());

        // DB에 저장
        messageRepository.save(newMessage);

        // WebSocket 세션으로 브로드캐스트
        broadcastMessage(roomId, newMessage);
    }




    private Long getRoomIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        Map<String, String> params = parseQueryString(query);
        return Long.valueOf(params.get("room_id"));
    }

    private Map<String, String> parseQueryString(String query) {
        Map<String, String> map = new ConcurrentHashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            map.put(keyValue[0], keyValue[1]);
        }
        return map;
    }

    private void broadcastMessage(Long roomId, Message message) throws Exception {
        List<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            MessageOutputDTO dto = convertToDTO(message);
            String payload = objectMapper.writeValueAsString(dto);

            // 닫힌 세션 추적
            List<WebSocketSession> closedSessions = new ArrayList<>();
            for (WebSocketSession session : sessions) {
                try {
                    session.sendMessage(new TextMessage(payload));
                } catch (Exception e) {
                    closedSessions.add(session); // 닫힌 세션 기록
                }
            }

            // 닫힌 세션 제거
            sessions.removeAll(closedSessions);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
    }




    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long roomId = getRoomIdFromSession(session);
        List<WebSocketSession> sessions = roomSessions.get(roomId);

        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
    }

}
