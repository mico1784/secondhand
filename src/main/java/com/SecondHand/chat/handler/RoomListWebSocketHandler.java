package com.SecondHand.chat.handler;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class RoomListWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void handleTextMessage(WebSocketSession session, org.springframework.web.socket.TextMessage message) throws Exception {
        // 채팅방 목록 업데이트 메시지 처리
        String payload = message.getPayload();

        if (payload.equals("updateRoomList")) {
            // 채팅방 목록 업데이트 로직
            updateRoomList();
        }
    }

    private void updateRoomList() {
        // 채팅방 목록을 클라이언트에 전송하는 로직을 추가
        // 예: 클라이언트에 채팅방 목록 JSON 전송
    }
}