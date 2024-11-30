package com.SecondHand.chat.room;

import com.SecondHand.chat.chatMessage.ChatMessageDTO;
import com.SecondHand.chat.chatMessage.DateUtils;

import lombok.Data;

@Data
public class RoomDTO {
    private Long id;
    private String roomNo;
    private Long sellerId;
    private String sellerName;
    private Long buyerId;
    private String buyerName;
    private ChatMessageDTO latestMessage;
    private String lastMessageTime;
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
        } else {
            this.latestMessage = null;
            this.lastMessageTime = null;
        }

        // Item 정보를 대신하여 itemId만 포함
        this.itemId = room.getItemId();
    }
}