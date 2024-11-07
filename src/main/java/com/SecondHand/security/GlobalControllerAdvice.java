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

        // 로그인 여부 확인
        boolean isLoggedIn = (principal != null || session.getAttribute("username") != null);
        request.setAttribute("isLoggedIn", isLoggedIn);

        // 사용자 정보 설정
        String username = null;
        boolean isGoogleUser = session.getAttribute("isGoogleUser") != null
                && (boolean) session.getAttribute("isGoogleUser");
        String name = null;

        if (principal != null) {
            // 일반 로그인 또는 구글 로그인 사용자 처리
            username = principal.getName();
            name = isGoogleUser ? (String) session.getAttribute("name") : username;
        } else if (session.getAttribute("username") != null) {
            // 카카오 로그인 사용자
            username = (String) session.getAttribute("username");
            name = isGoogleUser ? (String) session.getAttribute("name") : username;
        }

        // 속성 추가
        request.setAttribute("username", username);
        request.setAttribute("isGoogleUser", isGoogleUser);
        request.setAttribute("name", name);
    }
}
