package com.SecondHand.chat.room;

import com.SecondHand.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    Room findByRoomNo(String roomNo);
    @Query("SELECT r FROM Room r WHERE r.itemId = :itemId AND (r.buyerId = :buyerId OR r.sellerId = :sellerId)")
    Room findRoom(@Param("itemId") Long itemId, @Param("buyerId") Long buyerId, @Param("sellerId") Long sellerId);
    @Query("SELECT r FROM Room r WHERE r.sellerId = :userId OR r.buyerId = :userId")
    List<Room> findByUserId(@Param("userId") Long userId);
}
