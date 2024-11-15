package com.SecondHand.chat.room;

import com.SecondHand.chat.chatMessage.ChatMessage;
import com.SecondHand.item.Item;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String roomNo;
    private String roomName;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item itemC;  // itemC로 유지

    // Item과 roomNo를 받는 생성자 추가
    public Room(Item itemC, String roomNo) {
        this.itemC = itemC;  // itemC를 설정
        this.roomNo = roomNo;
        this.roomName = "Room " + roomNo;  // roomName은 roomNo 기반으로 설정 (선택사항)
    }

    public Room() {
        // 기본 생성자 필요 (JPA에서 엔티티를 관리하기 위해 필요)
    }
}
