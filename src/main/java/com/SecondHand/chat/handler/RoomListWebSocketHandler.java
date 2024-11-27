package com.SecondHand.chat.handler;

import com.SecondHand.chat.room.Room;
import com.SecondHand.chat.room.RoomDTO;
import com.SecondHand.chat.room.RoomRepository;
import com.SecondHand.item.ItemRepository;
import com.SecondHand.user.UserRepository;
import com.SecondHand.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final Set<WebSocketSession> sessions = new HashSet<>();

    @Autowired
    public RoomListWebSocketHandler(RoomRepository roomRepository, ItemRepository itemRepository, UserRepository userRepository, ObjectMapper objectMapper) {
        this.roomRepository = roomRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        sendRoomListToSession(session); // 새 연결에 방 목록 전송
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 클라이언트에서 별도 메시지 처리 로직이 필요하면 추가
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

            RoomDTO dto = new RoomDTO(room, null, sellerName, buyerName); // 최신 메시지는 null로 처리

            roomDtos.add(dto);
        }

        String payload = objectMapper.writeValueAsString(new RoomListResponse("updateRoomList", roomDtos));
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(payload));
            }
        }
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

            RoomDTO dto = new RoomDTO(room, null, sellerName, buyerName); // 최신 메시지는 null로 처리

            roomDtos.add(dto);
        }

        String payload = objectMapper.writeValueAsString(new RoomListResponse("updateRoomList", roomDtos));
        session.sendMessage(new TextMessage(payload));
    }

    // WebSocket 응답 클래스
    private static class RoomListResponse {
        private String type;
        private List<RoomDTO> rooms;

        public RoomListResponse(String type, List<RoomDTO> rooms) {
            this.type = type;
            this.rooms = rooms;
        }

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<RoomDTO> getRooms() {
            return rooms;
        }

        public void setRooms(List<RoomDTO> rooms) {
            this.rooms = rooms;
        }
    }
}
