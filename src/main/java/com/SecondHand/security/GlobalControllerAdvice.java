package com.SecondHand.security;

import com.SecondHand.user.User;
import com.SecondHand.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.Enumeration;

@ControllerAdvice // 모든 컨트롤러에 적용될 공통 설정을 위한 어드바이스 클래스
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository; // 사용자 정보를 데이터베이스에서 가져오기 위한 UserRepository 주입

    @ModelAttribute // 모든 요청에 대해 실행되는 메서드
    public void addAttributes(HttpServletRequest request, Principal principal) {
        HttpSession session = request.getSession(); // 현재 세션을 가져옴
        Enumeration<String> attributeNames = session.getAttributeNames(); // 세션에 저장된 모든 속성 이름 가져오기

        // 사용자가 로그인했는지 확인
        boolean isLoggedIn = (principal != null || session.getAttribute("username") != null);
        request.setAttribute("isLoggedIn", isLoggedIn); // 로그인 여부를 요청 속성에 추가

        // 사용자 이름 및 정보 초기화
        String username = null;
        boolean isGoogleUser = session.getAttribute("isGoogleUser") != null
                && (boolean) session.getAttribute("isGoogleUser"); // 구글 사용자 여부 확인
        String name = null;
        User user = null;

        // 사용자 정보 설정: 일반 로그인 또는 구글 로그인 사용자 처리
        if (principal != null) {
            username = principal.getName(); // Principal에서 사용자 이름 가져오기
            user = userRepository.findByUsername(username).orElse(null); // 사용자 정보 조회
            if (user != null) {
                name = user.getName(); // 사용자 이름 설정
            } else {
                name = isGoogleUser ? (String) session.getAttribute("name") : username; // 구글 사용자 이름 설정
            }
        } else if (session.getAttribute("username") != null) {
            // 세션에 저장된 사용자 이름이 있는 경우 (카카오 로그인)
            username = (String) session.getAttribute("username");
            user = userRepository.findByUsername(username).orElse(null); // 사용자 정보 조회
            if (user != null) {
                name = user.getName(); // 사용자 이름 설정
            } else {
                name = isGoogleUser ? (String) session.getAttribute("name") : username; // 구글 사용자 이름 설정
            }
        }

        // 요청에 속성 추가
        request.setAttribute("username", username); // 사용자 이름 설정
        request.setAttribute("isGoogleUser", isGoogleUser); // 구글 사용자 여부 설정
        request.setAttribute("name", name); // 사용자 이름 설정
        request.setAttribute("user", user); // User 객체 설정
    }
}
