package com.SecondHand.chat.controller;

import com.SecondHand.chat.handler.SocketHandler;
import com.SecondHand.chat.room.Room;
import com.SecondHand.item.Item;
import com.SecondHand.item.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
public class MainController {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private SocketHandler socketHandler;

    @RequestMapping("/chat/{itemId}")
    public String chatView(@PathVariable Long itemId, Model m, Authentication auth, Principal principal){
        if(principal != null){
            String username = auth.getName();
            m.addAttribute("username", username);

            Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Invalid item ID"));
            Room room = socketHandler.createOrGetRoom(item);
            System.out.println("RoomNo: " + room.getRoomNo());

            m.addAttribute("roomNo", room.getRoomNo());
            m.addAttribute("item", item);

            return "chat";
        }else{
            return "redirect:/login";
        }

    }
}
