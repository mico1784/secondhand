package com.SecondHand.chat.chatMessage;

import com.SecondHand.chat.room.Room;
import com.nimbusds.openid.connect.sdk.claims.PersonClaims;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String content;
    private String timestamp;
    private String sessionId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

}
