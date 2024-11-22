package com.SecondHand.chat.chatMessage;

import com.SecondHand.chat.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomOrderByTimestampAsc(Room room);
    ChatMessage findFirstByRoomIdOrderByIdDesc(Long roomId);

}
