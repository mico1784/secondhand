package com.SecondHand.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    private UserDetailsService userDetailsService;


    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    // 카카오 로그인 요청 메서드

    @GetMapping("/oauth/kakao")
    public String kakaoLogin() {
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
            System.out.println("Access Token: " + accessToken);

            // 사용자 정보 요청
            Map<String, Object> userInfo = kakaoService.getKakaoUserInfo(accessToken);
            Long kakaoId = (Long) userInfo.get("id");
            String kakaoUsername = (String) userInfo.get("nickname");

            // SecurityContext에 인증 정보 설정
            CustomUser kakaoUser = new CustomUser(
                    kakaoUsername,
                    "", // 비밀번호는 빈 문자열로 설정
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            kakaoUser.username = kakaoUsername;
            kakaoUser.id = kakaoId;

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    kakaoUser,
                    null,
                    kakaoUser.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 세션에 SecurityContext 설정
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // 세션에 사용자 정보 저장
            session.setAttribute("user", kakaoUser);
            session.setAttribute("username", kakaoUsername);

            return "redirect:/home";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login?error=exception"; // 예외 발생 시 로그인 페이지로 리다이렉트
        }
    }




}
