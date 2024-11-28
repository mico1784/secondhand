package com.SecondHand.chat.room;

import com.SecondHand.chat.chatMessage.ChatMessage;
import com.SecondHand.chat.chatMessage.ChatMessageDTO;
import com.SecondHand.chat.chatMessage.DateUtils;  // DateUtils 임포트
import com.SecondHand.item.Item;
import lombok.Data;

@Data
public class RoomDTO {
    private Long id;
    private String roomNo;
    private Long sellerId;
    private String sellerName;
    private Long buyerId;
    private String buyerName;
    private ChatMessageDTO latestMessage;  // ChatMessageDTO 타입
    private String lastMessageTime;  // 추가된 lastMessageTime 필드
    private String lastMessageTime2;
    private Long itemId;

    public RoomDTO(Room room, ChatMessageDTO latestMessage, String sellerName, String buyerName) {
        this.id = room.getId();
        this.roomNo = room.getRoomNo();
        this.sellerId = room.getSellerId();
        this.sellerName = sellerName;
        this.buyerId = room.getBuyerId();
        this.buyerName = buyerName;

        // ChatMessageDTO로 최신 메시지 처리
        if (latestMessage != null) {
            this.latestMessage = latestMessage;
            // timestamp를 포맷팅하여 lastMessageTime에 할당
            this.lastMessageTime = DateUtils.formatTimestamp(latestMessage.getTimestamp());
            this.lastMessageTime2 = latestMessage.getTimestamp();
        } else {
            this.latestMessage = null;
            this.lastMessageTime = null;
        }

        // Item 정보를 대신하여 itemId만 포함
        this.itemId = room.getItemC() != null ? room.getItemC().getId() : null;
    }
}