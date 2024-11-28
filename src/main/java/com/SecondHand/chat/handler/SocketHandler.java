package com.SecondHand.chat.handler;

import com.SecondHand.chat.Base64.Base64MultipartFile;
import com.SecondHand.chat.chatMessage.ChatMessage;
import com.SecondHand.chat.chatMessage.ChatMessageRepository;
import com.SecondHand.chat.room.Room;
import com.SecondHand.chat.room.RoomRepository;
import com.SecondHand.chat.room.RoomService;
import com.SecondHand.item.Item;
import com.SecondHand.item.ItemRepository;
import com.SecondHand.item.S3Service;
import com.SecondHand.user.User;
import com.SecondHand.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class SocketHandler extends TextWebSocketHandler {

    private final ChatMessageRepository chatMessageRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService; // RoomService 사용
    private final S3Service s3Service;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final RoomListWebSocketHandler roomListWebSocketHandler; // RoomListWebSocketHandler 주입

    private final Map<String, WebSocketSession> sessionMap = new HashMap<>();
    private final Map<String, Map<String, WebSocketSession>> roomSessionMap = new HashMap<>();
    private final Map<String, List<String>> userRoomMap = new HashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String msg = message.getPayload();
        Map<String, Object> obj = parseJsonToMap(msg);

        if (obj == null || !obj.containsKey("type")) return;

        String messageType = (String) obj.get("type");

        switch (messageType) {
            case "joinRoom":
                handleJoinRoom(session, obj);
                break;
            case "message":
                handleTextMessageProcessing(session, obj);
                break;
            case "image":
                handleImageMessage(session, obj);
                break;
            default:
                break;
        }
    }

    private void handleJoinRoom(WebSocketSession session, Map<String, Object> obj) {
        String roomNo = (String) obj.get("roomNo");
        String username = (String) obj.get("userName");
        userRoomMap.computeIfAbsent(username, k -> new ArrayList<>()).add(roomNo);

        // itemId 변환 처리
        String itemIdStr = (String) obj.get("itemId");
        Long itemId;
        try {
            itemId = Long.parseLong(itemIdStr);
        } catch (NumberFormatException e) {
            sendErrorMessageToClient("Invalid itemId", "The itemId provided is not valid.");
            return;
        }

        try {
            // RoomService를 사용하여 방 생성 또는 조회
            Room room = roomService.getRoomOrCreate(roomNo, itemId, username);
            session.getAttributes().put("roomNo", room.getRoomNo());
            session.getAttributes().put("itemId", room.getItemC().getId());

            roomSessionMap.computeIfAbsent(room.getRoomNo(), k -> new HashMap<>())
                    .put(session.getId(), session);

            // 채팅 기록을 클라이언트에게 전송
            sendChatHistory(session, roomNo);

        } catch (Exception e) {
            sendErrorMessageToClient("Room join failed", "An error occurred: " + e.getMessage());
        }
    }

    private void handleTextMessageProcessing(WebSocketSession session, Map<String, Object> obj) {
        try {
            String roomNo = (String) session.getAttributes().get("roomNo");
            Long itemId = (Long) session.getAttributes().get("itemId");
            if (itemId == null) return;

            String userName = (String) obj.get("userName");
            User buyer = userRepository.findByName(userName)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Long buyerId = buyer.getId();

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new NoSuchElementException("Item not found"));
            User seller = item.getSeller();
            Long sellerId = seller.getId();

            // RoomService를 사용하여 방 생성 또는 조회
            Room room = roomService.getRoomOrCreate(roomNo, itemId, userName);

            String clientSessionId = (String) obj.get("sessionId");

            ChatMessage chatMessage = createChatMessage(obj, room, clientSessionId);
            saveChatMessage(chatMessage);
            sendMessageToRoom(room.getRoomNo(), obj, chatMessage, clientSessionId);

            // 메시지 전송 후 방 목록 갱신
            roomListWebSocketHandler.updateRoomListAfterMessageSent(chatMessage);

        } catch (Exception e) {
            sendErrorMessageToClient("Error processing message", "An error occurred while processing the message: " + e.getMessage());
        }
    }

    private ChatMessage createChatMessage(Map<String, Object> obj, Room room, String clientSessionId) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender((String) obj.get("userName"));
        chatMessage.setContent((String) obj.get("msg"));
        chatMessage.setTimestamp(LocalDateTime.now().toString());
        chatMessage.setSessionId(clientSessionId);
        chatMessage.setRoom(room);
        return chatMessage;
    }

    private void saveChatMessage(ChatMessage chatMessage) {
        try {
            chatMessageRepository.save(chatMessage);
        } catch (Exception e) {
            sendErrorMessageToClient("Message save failed", "Error saving message: " + e.getMessage());
        }
    }

    private void sendMessageToRoom(String roomNo, Map<String, Object> obj, ChatMessage chatMessage, String clientSessionId) {
        if (!roomSessionMap.containsKey(roomNo)) {
            sendErrorMessageToClient("Room session not found", "No active sessions found for room " + roomNo);
            return;
        }

        Map<String, WebSocketSession> roomSessions = roomSessionMap.get(roomNo);
        for (WebSocketSession wss : roomSessions.values()) {
            try {
                obj.put("type", "message");
                obj.put("timestamp", chatMessage.getTimestamp());
                obj.put("sessionId", clientSessionId);
                wss.sendMessage(new TextMessage(new JSONObject(obj).toJSONString()));
            } catch (Exception e) {
                sendErrorMessageToClient("Message delivery failed", "Error sending message: " + e.getMessage());
            }
        }
    }

    // 이미지 메시지 처리
    private void handleImageMessage(WebSocketSession session, Map<String, Object> obj) {
        String base64Image = (String) obj.get("imageData");
        String userName = (String) obj.get("userName");
        String roomNo = (String) session.getAttributes().get("roomNo");

        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
                MultipartFile file = new Base64MultipartFile(imageBytes, "image.png");
                String imageUrl = s3Service.uploadFile(file);
                sendImageToRoom(roomNo, userName, imageUrl);
            } catch (Exception e) {
                sendErrorMessageToClient("Image upload failed", "Error uploading image: " + e.getMessage());
            }
        } else {
            sendErrorMessageToClient("Invalid image data", "No image data received.");
        }
    }

    private void sendImageToRoom(String roomNo, String userName, String imageUrl) {
        Map<String, Object> imageMessage = new HashMap<>();
        imageMessage.put("type", "image");
        imageMessage.put("userName", userName);
        imageMessage.put("imageUrl", imageUrl);

        roomSessionMap.getOrDefault(roomNo, Collections.emptyMap())
                .values()
                .forEach(session -> sendMessage(session, imageMessage));
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            session.sendMessage(new TextMessage(new JSONObject(message).toJSONString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessionMap.put(session.getId(), session);
        System.out.println(session);

        // URI에서 roomNo 추출
        String roomNoStr = extractRoomNoFromUri(session.getUri().toString());
        if (roomNoStr == null) {
            sendErrorMessageToClient("Room not found", "Room information could not be found.");
            return;
        }
        System.out.println("ROOM NUM : " + roomNoStr);
        session.getAttributes().put("roomNo", roomNoStr);
    }

    private void sendErrorMessageToClient(String errorType, String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("type", "error");
        errorResponse.put("errorType", errorType);
        errorResponse.put("errorMessage", errorMessage);
        // Send the error message to the client
    }

    // 채팅 기록을 클라이언트로 전송하는 메서드
    private void sendChatHistory(WebSocketSession session, String roomNoStr) {
        Room room = roomRepository.findByRoomNo(roomNoStr);
        if (room != null) {
            List<ChatMessage> chatHistory = chatMessageRepository.findByRoomOrderByTimestampAsc(room);
            chatHistory.forEach(chat -> {
                Map<String, Object> historyObj = new HashMap<>();
                historyObj.put("type", "message");
                historyObj.put("userName", chat.getSender());
                historyObj.put("msg", chat.getContent());
                historyObj.put("timestamp", chat.getTimestamp());
                historyObj.put("sessionId", chat.getSessionId());
                sendMessage(session, historyObj);
            });
        }
    }

    // URI에서 roomNo를 추출하는 메서드
    private String extractRoomNoFromUri(String uri) {
        try {
            URI fullUri = new URI(uri);
            return fullUri.getPath().split("/")[2];
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> parseJsonToMap(String msg) {
        try {
            return (Map<String, Object>) new JSONParser().parse(msg);
        } catch (ParseException e) {
            return null;
        }
    }
}
