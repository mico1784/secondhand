package com.SecondHand.chat.handler;

import com.SecondHand.chat.chatMessage.ChatMessage;
import com.SecondHand.chat.chatMessage.ChatMessageRepository;
import com.SecondHand.chat.room.Room;
import com.SecondHand.chat.room.RoomRepository;
import com.SecondHand.item.Item;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SocketHandler extends TextWebSocketHandler {

    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private RoomRepository roomRepository;

    // WebSocket session을 저장하는 맵
    HashMap<String, WebSocketSession> sessionMap = new HashMap<>();
    HashMap<Integer, HashMap<String, WebSocketSession>> roomSessionMap = new HashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String msg = message.getPayload(); // 클라이언트가 보낸 메시지
        HashMap<String, Object> obj = jsonToObjectParser(msg);

        if (obj == null || !obj.containsKey("type")) {
            return; // 유효하지 않은 메시지 무시
        }

        String messageType = (String) obj.get("type");

        if ("joinRoom".equals(messageType)) {
            // 초기 연결 시 방 번호 설정
            Integer roomNo = Integer.parseInt(obj.get("roomNo").toString());
            session.getAttributes().put("roomNo", roomNo);
            roomSessionMap.computeIfAbsent(roomNo, k -> new HashMap<>()).put(session.getId(), session);

            System.out.println("세션에 방 번호 설정 완료: " + session.getId() + " 방 번호: " + roomNo);
            return; // joinRoom 처리 후 종료
        }

        // 일반 메시지 처리
        if ("message".equals(messageType)) {
            Integer roomNo = (Integer) session.getAttributes().get("roomNo");
            if (roomNo == null) {
                sendErrorMessageToClient("Room number not set", "Session does not have an associated room number.");
                return;
            }

            Room room = roomRepository.findByRoomNo(roomNo);
            if (room == null) {
                sendErrorMessageToClient("Room not found", "Room with ID " + roomNo + " does not exist.");
                return;
            }

            // 메시지 저장 및 전송
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSender((String) obj.get("userName"));
            chatMessage.setContent((String) obj.get("msg"));
            chatMessage.setTimestamp(LocalDateTime.now());
            chatMessage.setRoom(room);

            try {
                chatMessageRepository.save(chatMessage);
                System.out.println("메시지 db저장 완료");
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorMessageToClient("Message save failed", "Error saving message: " + e.getMessage());
                return;
            }

            if (roomSessionMap.containsKey(roomNo)) {
                HashMap<String, WebSocketSession> roomSessions = roomSessionMap.get(roomNo);
                for (WebSocketSession wss : roomSessions.values()) {
                    try {
                        obj.put("type", "message");
                        obj.put("sessionId", session.getId());
                        wss.sendMessage(new TextMessage(new JSONObject(obj).toJSONString()));
                    } catch (Exception e) {
                        System.out.println("메시지 전송 실패, " + e.getMessage());
                        e.printStackTrace();
                        sendErrorMessageToClient("Message delivery failed", "Error sending message: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("룸 세션을 찾을 수 없슴");
                sendErrorMessageToClient("Room session not found", "No active sessions found for room " + roomNo);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessionMap.put(session.getId(), session);   // 새로운 세션을 생성하며 Map에 추가함
        System.out.println("session: " + session);

        // URI에서 쿼리 파라미터로 받은 방 번호를 추출
        String uri = session.getUri().toString();
        String roomNoStr = extractRoomNoFromUri(uri);

        if (roomNoStr == null) {
            System.out.println("클라이언트에서 방 번호가 제공되지 않았습니다.");
            sendErrorMessageToClient("Room number missing", "No room number provided by client.");
            return;
        }

        try {
            Integer roomNo = Integer.parseInt(roomNoStr);
            session.getAttributes().put("roomNo", roomNo);  // 정수로 저장

            // roomSessionMap에 세션 추가
            roomSessionMap.computeIfAbsent(roomNo, k -> new HashMap<>()).put(session.getId(), session);

            // 로그 추가
            System.out.println("세션 추가됨: " + session.getId() + " 방 번호: " + roomNo);
            System.out.println("현재 roomSessionMap 상태: " + roomSessionMap);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            sendErrorMessageToClient("Invalid room number", "Room number must be a valid integer.");
            return;
        }

        // 세션 ID 전송
        HashMap<String, Object> obj = new HashMap<>();
        obj.put("type", "getId");
        obj.put("sessionId", session.getId());

        try {
            session.sendMessage(new TextMessage(new JSONObject(obj).toJSONString()));
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorMessageToClient("Connection error", "Error sending session ID: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionMap.remove(session.getId());         // 종료된 세션을 맵에서 삭제함
        roomSessionMap.forEach((roomNo, sessions) -> {
            if (sessions != null) {
                sessions.remove(session.getId());
            }
        });
        super.afterConnectionClosed(session, status);
        System.out.println("웹 소켓 닫힘: " + session.getId());
    }

    // 문자열 형태의 JSON을 HashMap으로 변환하는 메서드
    private static HashMap<String, Object> jsonToObjectParser(String jsonStr) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(jsonStr);
            HashMap<String, Object> result = new HashMap<>();

            // JSONObject의 모든 항목을 HashMap에 수동으로 추가
            for (Object key : jsonObject.keySet()) {
                result.put((String) key, jsonObject.get(key));
            }

            return result;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 클라이언트에게 오류 메시지 전송
    private void sendErrorMessageToClient(String errorMessage, String details) {
        // 타입을 명시적으로 지정한 HashMap 사용
        HashMap<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("type", "error");
        errorResponse.put("message", errorMessage);
        errorResponse.put("details", details);

        // 연결된 세션들에게 에러 메시지 전송
        roomSessionMap.forEach((roomNo, sessions) -> {
            for (WebSocketSession session : sessions.values()) {
                try {
                    session.sendMessage(new TextMessage(new JSONObject(errorResponse).toJSONString()));
                } catch (Exception e) {
                    e.printStackTrace(); // 에러 발생 시 로그 출력
                }
            }
        });
    }

    public Room createOrGetRoom(Item item) {
        Room room = roomRepository.findByItemC(item);

        if (room == null) {
            room = new Room();
            room.setRoomNo(generateRoomNo());
            room.setRoomName(item.getTitle() + "chatRoom");
            room.setItemC(item);
            room = roomRepository.save(room);
        }
        return room;
    }

    private Integer generateRoomNo() {
        return (int) (Math.random() * 100000);
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
}
