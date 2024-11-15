package com.SecondHand.chat.handler;

import com.SecondHand.chat.Base64.Base64MultipartFile;
import com.SecondHand.chat.chatMessage.ChatMessage;
import com.SecondHand.chat.chatMessage.ChatMessageRepository;
import com.SecondHand.chat.room.Room;
import com.SecondHand.chat.room.RoomRepository;
import com.SecondHand.item.Item;
import com.SecondHand.item.ItemRepository;
import com.SecondHand.item.S3Service;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
public class SocketHandler extends TextWebSocketHandler {

    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private S3Service s3Service;  // S3 서비스 추가
    @Autowired
    private ItemRepository itemRepository;

    HashMap<String, WebSocketSession> sessionMap = new HashMap<>();
    HashMap<String, HashMap<String, WebSocketSession>> roomSessionMap = new HashMap<>();
    private Map<String, Room> roomMap = new HashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String msg = message.getPayload(); // 클라이언트가 보낸 메시지
        HashMap<String, Object> obj = jsonToObjectParser(msg);

        if (obj == null || !obj.containsKey("type")) {
            return; // 유효하지 않은 메시지 무시
        }

        String messageType = (String) obj.get("type");

        if ("joinRoom".equals(messageType)) {
            String roomNo = obj.get("roomNo").toString();
            session.getAttributes().put("roomNo", roomNo);
            roomSessionMap.computeIfAbsent(roomNo, k -> new HashMap<>()).put(session.getId(), session);
            return; // joinRoom 처리 후 종료
        }

        if ("message".equals(messageType)) {
            handleTextMessageProcessing(session, obj);
        } else if ("image".equals(messageType)) {
            handleImageMessage(session, obj);
        }
    }

    private void handleTextMessageProcessing(WebSocketSession session, HashMap<String, Object> obj) {
        String roomNo = (String) session.getAttributes().get("roomNo");
        Object itemIdObj = obj.get("itemId");
        Long itemId = null;

        if (itemIdObj != null) {
            if (itemIdObj instanceof Long) {
                itemId = (Long) itemIdObj;  // 이미 Long 타입이면 바로 사용
            } else if (itemIdObj instanceof String) {
                try {
                    itemId = Long.parseLong((String) itemIdObj);  // String을 Long으로 변환
                } catch (NumberFormatException e) {
                    sendErrorMessageToClient("Invalid itemId", "The itemId is not a valid number.");
                    return;
                }
            } else {
                sendErrorMessageToClient("Invalid itemId type", "The itemId must be of type Long or String.");
                return;
            }
        } else {
            sendErrorMessageToClient("Item ID not found", "No itemId provided in the message.");
            return;
        }
        if (roomNo == null) {
            sendErrorMessageToClient("Room number not set", "Session does not have an associated room number.");
            return;
        }

        // 방을 찾고, 없으면 새로 생성
        Room room = roomRepository.findByRoomNo(roomNo);
        if (room == null) {
            // 방이 없으면 새로 생성
            room = new Room();
            room.setRoomNo(roomNo);
            room.setRoomName("채팅방 " + roomNo); // 방 이름은 필요에 맞게 설정
            room.setItemC(itemRepository.findById(itemId).orElseThrow(() -> new NoSuchElementException("Item not found")));
            roomRepository.save(room);  // 새로운 방 저장

            // 방을 생성 후 방 정보로 로직 진행
            System.out.println("New room created with roomNo: " + roomNo);
        }

        String clientSessionId = (String) obj.get("sessionId");

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender((String) obj.get("userName"));
        chatMessage.setContent((String) obj.get("msg"));
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setSessionId(clientSessionId);
        chatMessage.setRoom(room);

        try {
            chatMessageRepository.save(chatMessage);  // 메시지 저장
        } catch (Exception e) {
            sendErrorMessageToClient("Message save failed", "Error saving message: " + e.getMessage());
            return;
        }

        // 방에 연결된 세션들에게 메시지 전달
        if (roomSessionMap.containsKey(roomNo)) {
            HashMap<String, WebSocketSession> roomSessions = roomSessionMap.get(roomNo);
            for (WebSocketSession wss : roomSessions.values()) {
                try {
                    obj.put("type", "message");
                    obj.put("sessionId", clientSessionId);
                    wss.sendMessage(new TextMessage(new JSONObject(obj).toJSONString()));
                } catch (Exception e) {
                    sendErrorMessageToClient("Message delivery failed", "Error sending message: " + e.getMessage());
                }
            }
        } else {
            sendErrorMessageToClient("Room session not found", "No active sessions found for room " + roomNo);
        }
    }

    private void handleImageMessage(WebSocketSession session, HashMap<String, Object> obj) {
        String base64Image = (String) obj.get("imageData");
        String userName = (String) obj.get("userName");
        String roomNo = (String) session.getAttributes().get("roomNo");

        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                // base64 이미지를 byte[] 배열로 변환
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);

                // MultipartFile로 변환
                MultipartFile file = new Base64MultipartFile(imageBytes, "image.png"); // 파일 이름은 예시로 지정

                // S3에 업로드
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
        HashMap<String, Object> imageMessage = new HashMap<>();
        imageMessage.put("type", "image");
        imageMessage.put("userName", userName);
        imageMessage.put("imageUrl", imageUrl);

        roomSessionMap.forEach((room, sessions) -> {
            if (room.equals(roomNo)) {
                for (WebSocketSession wss : sessions.values()) {
                    try {
                        wss.sendMessage(new TextMessage(new JSONObject(imageMessage).toJSONString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessionMap.put(session.getId(), session);
        String uri = session.getUri().toString();
        String roomNoStr = extractRoomNoFromUri(uri);

        if (roomNoStr == null) {
            sendErrorMessageToClient("Room number missing", "No room number provided by client.");
            return;
        }

        try {
            session.getAttributes().put("roomNo", roomNoStr);

            roomSessionMap.computeIfAbsent(roomNoStr, k -> new HashMap<>()).put(session.getId(), session);

            Room room = roomRepository.findByRoomNo(roomNoStr);
            if (room != null) {
                List<ChatMessage> chatHistory = chatMessageRepository.findByRoomOrderByTimestampAsc(room);
                for (ChatMessage chat : chatHistory) {
                    HashMap<String, Object> historyObj = new HashMap<>();
                    historyObj.put("type", "message");
                    historyObj.put("userName", chat.getSender());
                    historyObj.put("msg", chat.getContent());
                    historyObj.put("timestamp", chat.getTimestamp().toString());
                    historyObj.put("sessionId", chat.getSessionId());
                    session.sendMessage(new TextMessage(new JSONObject(historyObj).toJSONString()));
                }
            }
        } catch (NumberFormatException e) {
            sendErrorMessageToClient("Invalid room number", "Room number must be a valid integer.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionMap.remove(session.getId());
        roomSessionMap.forEach((roomNo, sessions) -> sessions.remove(session.getId()));
        super.afterConnectionClosed(session, status);
    }

    private String extractRoomNoFromUri(String uri) {
        try {
            // URI 객체로 변환
            URI url = new URI(uri);

            // 쿼리 파라미터가 존재할 경우, 쿼리 파라미터를 Map 형태로 변환
            Map<String, String> queryParams = URI.create(uri).getQuery().lines()
                    .map(param -> param.split("="))
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));

            // 쿼리 파라미터에서 roomNo를 반환
            return queryParams.get("roomNo");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 오류 메시지를 클라이언트로 전송
    private void sendErrorMessageToClient(String errorMessage, String details) {
        HashMap<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("type", "error");
        errorResponse.put("message", errorMessage);
        errorResponse.put("details", details);

        roomSessionMap.forEach((roomNo, sessions) -> {
            for (WebSocketSession session : sessions.values()) {
                try {
                    session.sendMessage(new TextMessage(new JSONObject(errorResponse).toJSONString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // JSON 문자열을 Map으로 파싱
    private HashMap<String, Object> jsonToObjectParser(String jsonString) {
        try {
            // JSON을 파싱하여 Object로 반환
            Object obj = new JSONParser().parse(jsonString);

            // 반환된 Object가 Map 타입인지 확인 후 안전하게 캐스팅
            if (obj instanceof Map) {
                return new HashMap<String, Object>((Map<String, Object>) obj);  // 안전하게 캐스팅
            } else {
                throw new IllegalArgumentException("Parsed object is not a valid Map.");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
