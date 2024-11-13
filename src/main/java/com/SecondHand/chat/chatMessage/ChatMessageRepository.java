package com.SecondHand.chat.chatMessage;

import com.SecondHand.chat.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomOrderByTimestampAsc(Room room);
}
