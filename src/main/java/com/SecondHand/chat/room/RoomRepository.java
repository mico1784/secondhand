package com.SecondHand.chat.room;

import com.SecondHand.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    Room findByRoomNo(String roomNo);
    Room findByItemC(Item itemC);
}
