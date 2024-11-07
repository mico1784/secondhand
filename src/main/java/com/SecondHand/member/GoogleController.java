package com.SecondHand.member;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class GoogleController {
    @GetMapping("/oauth2/authorization/google")
    public String redirectToGoogleLogin() {
        return "redirect:/oauth2/authorization/google";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess() {
        // 로그인 성공 후 처리할 코드
        return "redirect:/home";
    }
}
