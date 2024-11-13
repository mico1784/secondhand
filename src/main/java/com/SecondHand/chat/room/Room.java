package com.SecondHand.chat.room;

import com.SecondHand.chat.chatMessage.ChatMessage;
import com.SecondHand.item.Item;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer roomNo;
    private String roomName;

    @OneToMany(mappedBy = "room")
    private List<ChatMessage> chatMessages;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item itemC;
}
