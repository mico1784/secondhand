package com.SecondHand.chat.room;

import com.SecondHand.chat.chatMessage.ChatMessage;
import com.SecondHand.item.Item;
import com.SecondHand.member.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;        // ID
    private String roomNo;  // 방 이름

    @Column(name = "seller_id")
    private Long sellerId;  // 판매자 정보
    @Column(name = "buyer_id")
    private Long buyerId;   // 구매자 정보

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages; // 채팅내용

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item itemC;  // 거래 물품
}
