package com.SecondHand.chat.handler;

import com.SecondHand.user.User;
import com.SecondHand.chat.chatMessage.ChatMessage;
import com.SecondHand.chat.chatMessage.ChatMessageDTO;
import com.SecondHand.chat.chatMessage.ChatMessageRepository;
import com.SecondHand.chat.room.Room;
import com.SecondHand.chat.room.RoomDTO;
import com.SecondHand.chat.room.RoomRepository;
import com.SecondHand.item.ItemRepository;
import com.SecondHand.user.UserRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class RoomListWebSocketHandler extends TextWebSocketHandler {

    private final RoomRepository roomRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ChatMessageRepository chatMessageRepository;  // 채팅 메시지 리포지토리
    private final Set<WebSocketSession> sessions = new HashSet<>();

    @Autowired
    public RoomListWebSocketHandler(RoomRepository roomRepository, ItemRepository itemRepository, UserRepository userRepository, ObjectMapper objectMapper, ChatMessageRepository chatMessageRepository) {
        this.roomRepository = roomRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        sendRoomListToSession(session); // 새 연결에 방 목록 전송
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    // 채팅방 목록을 전송하는 메서드
    public void broadcastRoomList() throws Exception {
        List<Room> rooms = roomRepository.findAll();
        List<RoomDTO> roomDtos = new ArrayList<>();

        for (Room room : rooms) {
            // 각 방에 대해 RoomDTO 객체 생성
            String sellerName = userRepository.findById(room.getSellerId())
                    .map(User::getName).orElse("Unknown");
            String buyerName = userRepository.findById(room.getBuyerId())
                    .map(User::getName).orElse("Unknown");

            // 최신 메시지 가져오기
            ChatMessage latestMessage = chatMessageRepository.findTopByRoomOrderByTimestampDesc(room);
            ChatMessageDTO latestMessageDTO = latestMessage != null ? new ChatMessageDTO(latestMessage) : null;

            RoomDTO dto = new RoomDTO(room, latestMessageDTO, sellerName, buyerName); // 최신 채팅 추가

            roomDtos.add(dto);
        }

        String payload = objectMapper.writeValueAsString(new RoomListResponse("updateRoomList", roomDtos));
        // 연결된 모든 세션에 채팅방 목록을 전송
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(payload));
            }
        }
    }

    // 채팅방 생성, 메시지 전송, 상태 변경 시 호출하여 목록을 업데이트
    public void updateRoomListAfterMessageSent(ChatMessage chatMessage) throws Exception {
        // 메시지 전송 후 방 목록을 업데이트
        broadcastRoomList();
    }

    // 새로운 연결이 있을 때 방 목록을 보내는 메서드
    private void sendRoomListToSession(WebSocketSession session) throws Exception {
        List<Room> rooms = roomRepository.findAll();
        List<RoomDTO> roomDtos = new ArrayList<>();

        for (Room room : rooms) {
            // 각 방에 대해 RoomDTO 객체 생성
            String sellerName = userRepository.findById(room.getSellerId())
                    .map(User::getName).orElse("Unknown");
            String buyerName = userRepository.findById(room.getBuyerId())
                    .map(User::getName).orElse("Unknown");

            // 최신 메시지 가져오기
            ChatMessage latestMessage = chatMessageRepository.findTopByRoomOrderByTimestampDesc(room);
            ChatMessageDTO latestMessageDTO = latestMessage != null ? new ChatMessageDTO(latestMessage) : null;

            RoomDTO dto = new RoomDTO(room, latestMessageDTO, sellerName, buyerName); // 최신 채팅 추가

            roomDtos.add(dto);
        }

        String payload = objectMapper.writeValueAsString(new RoomListResponse("updateRoomList", roomDtos));
        session.sendMessage(new TextMessage(payload));
    }

    // WebSocket 응답 클래스
    @Data
    private static class RoomListResponse {
        @JsonProperty("type")
        private String type;
        @JsonProperty("rooms")
        private List<RoomDTO> rooms;

        public RoomListResponse(String type, List<RoomDTO> rooms) {
            this.type = type;
            this.rooms = rooms;
        }
    }
}
