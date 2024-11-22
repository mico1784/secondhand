package com.SecondHand.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class GoogleController {

    @Autowired
    private UserRepository userRepository;

    // 구글 로그인 요청을 처리하는 메서드
    @GetMapping("/oauth2/authorization/google")
    public String redirectToGoogleLogin() {
        return "redirect:/oauth2/authorization/google";  // 구글 로그인 페이지로 리다이렉트
    }

    // 구글 로그인 콜백을 처리하는 메서드
    @GetMapping("/oauth2/authorization/google/callback")
    public String googleCallback(@RequestParam String code, HttpSession session) {
        try {
            // SecurityContext에서 현재 인증된 사용자의 OAuth2User 정보를 가져옴
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // 구글 사용자 정보 추출
            String googleId = oAuth2User.getAttribute("sub"); // 고유한 Google ID
            String username = (String) oAuth2User.getAttribute("name"); // 사용자 이름
            String email = (String) oAuth2User.getAttribute("email"); // 사용자 이메일

            // 데이터베이스에서 Google ID로 사용자 찾기, 없으면 새 사용자 생성
            User googleUser = userRepository.findByGoogleId(googleId)
                    .orElseGet(() -> {
                        // 새로운 사용자 생성 및 저장
                        User newUser = new User();
                        newUser.setUsername(username);
                        newUser.setEmail(email);
                        newUser.setGoogleId(googleId);
                        newUser.setRole("ROLE_USER"); // 기본 사용자 역할 설정
                        return userRepository.save(newUser);
                    });

            // 세션에 사용자 정보 저장
            session.setAttribute("user", googleUser);
            session.setAttribute("username", username);
            session.setAttribute("isGoogleUser", true); // 구글 사용자 여부 설정

            return "redirect:/home"; // 홈 화면으로 리다이렉트
        } catch (Exception e) {
            e.printStackTrace(); // 예외를 콘솔에 출력
            return "redirect:/login?error=exception"; // 예외 발생 시 로그인 페이지로 리다이렉트
        }
    }

    // 페이지에서 사용자 정보를 가져와 세션에 추가하는 메서드
    @ModelAttribute
    public void addAttributes(HttpSession session, Authentication authentication) {
        String username = null;
        String name = null;
        boolean isGoogleUser = session.getAttribute("isGoogleUser") != null
                && (boolean) session.getAttribute("isGoogleUser");

        // 사용자가 인증된 상태인 경우 정보 추출
        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName(); // 인증된 사용자의 username
            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                name = oAuth2User.getAttribute("name"); // 구글에서 제공된 사용자 이름
            }
        }

        // 세션에 사용자 정보 저장
        session.setAttribute("username", username);
        session.setAttribute("name", name);
        session.setAttribute("isGoogleUser", isGoogleUser);
    }
}
