package com.SecondHand.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addAttributes(HttpServletRequest request, Principal principal) {
        HttpSession session = request.getSession();

        // 로그인 여부 확인: 일반 로그인 또는 카카오 로그인 세션 체크
        boolean isLoggedIn = (principal != null || session.getAttribute("username") != null);
        request.setAttribute("isLoggedIn", isLoggedIn);

        // 사용자 이름 설정: 일반 로그인은 principal, 카카오는 세션에서 가져옴
        String username = null;
        if (principal != null) {
            username = principal.getName();
        } else if (session.getAttribute("username") != null) {
            username = (String) session.getAttribute("username");
        }

        request.setAttribute("username", username);
    }
}
