package com.SecondHand.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@Controller
public class KakaoController {

    @Autowired
    private KakaoService kakaoService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("principalDetailsService")
    private UserDetailsService userDetailsService;

    @Value("${kakao.client-id}")
    private String clientId; // 카카오 클라이언트 ID

    @Value("${kakao.redirect-uri}")
    private String redirectUri; // 카카오 리다이렉트 URI

    // 카카오 로그인 요청 메서드
    @GetMapping("/oauth/kakao")
    public String kakaoLogin() {
        // 카카오 로그인 요청 URL 생성 및 리다이렉트
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize?client_id="
                + clientId + "&redirect_uri=" + redirectUri + "&response_type=code";
        return "redirect:" + kakaoAuthUrl;
    }

    // 카카오 로그인 콜백 메서드
    @GetMapping("/oauth/kakao/callback")
    public String kakaoCallback(@RequestParam String code, HttpSession session) {
        try {
            // 액세스 토큰 발급
            String accessToken = kakaoService.getKakaoAccessToken(code);

            // 사용자 정보 요청
            Map<String, Object> userInfo = kakaoService.getKakaoUserInfo(accessToken);
            Long kakaoId = (Long) userInfo.get("id"); // 카카오 ID
            String kakaoUsername = (String) userInfo.get("nickname"); // 카카오 닉네임

            System.out.println("Kakao User Info - ID: " + kakaoId + ", Username: " + kakaoUsername);

            // 카카오 ID로 사용자 조회, 없으면 새로 생성
            User kakaoUser = userRepository.findByKakaoId(kakaoId)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername(kakaoUsername); // 사용자 이름 설정
                        newUser.setName(kakaoUsername); // 사용자 이름 설정
                        newUser.setKakaoId(kakaoId); // 카카오 ID 설정
                        newUser.setRole("USER"); // 기본 역할 설정
                        return userRepository.save(newUser); // 저장
                    });

            // SecurityContext에 인증 정보 설정
            CustomUser customUser = new CustomUser(
                    kakaoUser.getUsername(),
                    "", // 비밀번호는 빈 문자열로 설정
                    List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 설정
            );
            customUser.setCustomUsername(kakaoUser.getUsername());
            customUser.setId(kakaoUser.getId());
            customUser.setKakaoId(kakaoUser.getKakaoId());

            // 인증 토큰 생성 및 설정
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    customUser,
                    null,
                    customUser.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 세션에 사용자 정보 저장
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            session.setAttribute("user", customUser);
            session.setAttribute("username", kakaoUsername);

            return "redirect:/home"; // 홈 페이지로 리다이렉트

        } catch (Exception e) {
            e.printStackTrace(); // 예외 로그 출력
            return "redirect:/login?error=exception"; // 예외 시 로그인 페이지로 리다이렉트
        }
    }
}
