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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ChatMessageRepository chatMessageRepository;  // 채팅 메시지 리포지토리
    private final Set<WebSocketSession> sessions = new HashSet<>();

    @Autowired
    public RoomListWebSocketHandler(RoomRepository roomRepository, ItemRepository itemRepository, UserRepository userRepository, ObjectMapper objectMapper, ChatMessageRepository chatMessageRepository) {
        this.roomRepository = roomRepository;
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
    private void broadcastRoomList() throws Exception {
        for (WebSocketSession session : sessions) {
            try {
                // 각 세션의 사용자 정보 가져오기
                String username = session.getPrincipal().getName();
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
                Long userId = user.getId();

                // 사용자와 관련된 방만 가져오기
                List<Room> rooms = roomRepository.findByUserId(userId);
                List<RoomDTO> roomDtos = new ArrayList<>();

                for (Room room : rooms) {
                    String sellerName = userRepository.findById(room.getSellerId())
                            .map(User::getName).orElse("Unknown");
                    String buyerName = userRepository.findById(room.getBuyerId())
                            .map(User::getName).orElse("Unknown");

                    ChatMessage latestMessage = chatMessageRepository.findTopByRoomOrderByTimestampDesc(room);
                    ChatMessageDTO latestMessageDTO = latestMessage != null ? new ChatMessageDTO(latestMessage) : null;

                    RoomDTO dto = new RoomDTO(room, latestMessageDTO, sellerName, buyerName);
                    roomDtos.add(dto);
                }

                roomDtos.sort((a, b) -> {
                    String timeA = a.getLatestMessage() != null ? a.getLatestMessage().getTimestamp() : "0000-01-01T00:00:00.000000000";
                    String timeB = b.getLatestMessage() != null ? b.getLatestMessage().getTimestamp() : "0000-01-01T00:00:00.000000000";
                    return timeB.compareTo(timeA);
                });

                String payload = objectMapper.writeValueAsString(new RoomListResponse("updateRoomList", roomDtos));
                session.sendMessage(new TextMessage(payload));
            } catch (Exception e) {
                e.printStackTrace();
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
        // WebSocketSession에서 Principal 가져오기
        String username = session.getPrincipal().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Long userId = user.getId();

        List<Room> rooms = roomRepository.findByUserId(userId);
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
