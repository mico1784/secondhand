package com.SecondHand.chat.room;

import com.SecondHand.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    Room findByRoomNo(String roomNo);
    Room findByItemC_Id(Long itemId);
    @Query("SELECT r FROM Room r WHERE r.sellerId = :userId OR r.buyerId = :userId")
    List<Room> findByUserId(@Param("userId") Long userId);
}
