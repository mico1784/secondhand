package com.SecondHand.chat.config;

import com.SecondHand.chat.handler.SocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SocketHandler socketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){
        registry.addHandler(socketHandler, "/chatting");
    }
}

    //  /chatting 경로에서 웹 소켓 연결을 처리하도록 설정
    //  클라이언트가 해당 경로로 연결 요청하면 socketHandler가 연결 관리, 메시지 처리함
    //  WebSocket은 실시간 양방향 소통이 가능하게 해주는 프로토콜, 채팅이나 실시간 알림 등에 사용
