package com.SecondHand.chat.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
public class MainController {

    @RequestMapping("/chat")
    public String chatView(Model m, Authentication auth, Principal principal){
        if(principal != null){
            String username = auth.getName();
            m.addAttribute("username", username);

            System.out.println(username);

            return "chat";
        }else{
            return "redirect:/login";
        }

    }
}
