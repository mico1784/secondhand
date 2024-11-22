package com.SecondHand.chat.chatMessage;

import com.SecondHand.chat.room.Room;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long id;
    private String sender;
    private String content;
    private String timestamp;
    private String sessionId;
    private Long roomId;

    public ChatMessageDTO(ChatMessage chatMessage){
        this.id = chatMessage.getId();
        this.sender = chatMessage.getSender();
        this.content = chatMessage.getContent();
        this.timestamp = chatMessage.getTimestamp();
        this.sessionId = chatMessage.getSessionId();
        this.roomId = chatMessage.getRoom().getId();
    }
}
