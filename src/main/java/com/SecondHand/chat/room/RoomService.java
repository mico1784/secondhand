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

    public Room getRoomOrCreate(String roomNo, Long itemId, String username) {
        Room room = roomRepository.findByRoomNo(roomNo);
        if (room == null) {
            room = new Room();
            room.setRoomNo(roomNo);

            // 아이템 설정
            Item item = itemRepository.findById(itemId).orElseThrow(() -> new NoSuchElementException("Item not found"));
            room.setItemC(item);

            // sellerId와 buyerId를 username을 통해 찾아서 설정
            User buyer = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            room.setBuyerId(buyer.getId());
            User seller = item.getSeller();
            room.setSellerId(seller.getId());

            // 방 저장
            roomRepository.save(room);
        }
        return room;
    }
}
