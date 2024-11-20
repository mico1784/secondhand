package com.SecondHand.chat.room;

import com.SecondHand.item.Item;
import com.SecondHand.item.ItemRepository;
import com.SecondHand.member.User;
import com.SecondHand.member.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository, ItemRepository itemRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    // 방이 없으면 생성하고 반환하는 메서드
    public Room getRoomOrCreate(String roomNo, Long itemId, String username) {
        Room room = roomRepository.findByRoomNo(roomNo);  // 먼저 방 번호로 조회
        if (room == null) {  // 방이 없다면 새로 생성
            // 방을 새로 생성하기 위한 필수 정보들 확인
            if (itemId == null || username == null) {
                throw new IllegalArgumentException("Item ID or Username cannot be null");
            }

            room = new Room();
            room.setRoomNo(roomNo);  // 방 번호 설정

            // 아이템 설정 (아이템이 없으면 예외 처리)
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new NoSuchElementException("Item not found"));
            room.setItemC(item);  // 아이템 설정

            // User 찾기 (Buyer)
            User buyer = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            room.setBuyerId(buyer.getId());  // Buyer ID 설정

            // Seller 설정 (아이템에 해당하는 seller로 설정)
            User seller = item.getSeller();
            room.setSellerId(seller.getId());  // Seller ID 설정

            // 방 저장
            roomRepository.save(room);
        }
        return room;  // 방을 반환
    }
}