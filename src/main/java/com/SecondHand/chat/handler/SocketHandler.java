package com.SecondHand.chat.handler;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;

@Component
public class SocketHandler extends TextWebSocketHandler {

    // WebSocket session을 저장하는 맵
    HashMap<String, WebSocketSession> sessionMap = new HashMap<>();

    @Override   // 메시지 전송
    public void handleTextMessage(WebSocketSession session, TextMessage message){
        String msg = message.getPayload();  // 클라이언트가 보낸 메시지를 문자열로 가져옴
        JSONObject obj = jsonToObjectParser(msg);   // 받아온 문자열 메시지를 JSON으로 변환함
        for(String key : sessionMap.keySet()){      // sessionMap에 있는 모든 세션에 반복 = 모든 클라이언트에게 메시지 전송
            WebSocketSession wss = sessionMap.get(key);
            try {
                wss.sendMessage(new TextMessage(obj.toJSONString())); // JSON을 다시 문자열로 변환해서 전송
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override   // 소켓 연결
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        super.afterConnectionEstablished(session);
        sessionMap.put(session.getId(), session);   // 새로운 세션을 생성하며 Map에 추가함
        JSONObject obj = new JSONObject();          // JSON 객체를 생성하여 클라이언트에게 고유한 세션ID를 전송함 => 메시지 전송이나 사용자 구분에 씀
        obj.put("type", "getId");
        obj.put("sessionId", session.getId());
        session.sendMessage(new TextMessage(obj.toJSONString()));
    }

    @Override   // 소켓 종료
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
        sessionMap.remove(session.getId());         // 종료된 세션을 맵에서 삭제함
        super.afterConnectionClosed(session, status);
    }

    //  문자열 형태의 JSON을 JSONObject로 변환하는 메서드
    private static JSONObject jsonToObjectParser(String jsonStr){
        JSONParser parser = new JSONParser();
        JSONObject obj = null;

        try{
            obj = (JSONObject)parser.parse(jsonStr);
        }catch (ParseException e){
            e.printStackTrace();
        }
        return obj ;
    }

}
