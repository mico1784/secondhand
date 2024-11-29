package com.SecondHand.user;

import org.springframework.security.oauth2.core.OAuth2Error;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    @Lazy
    private BCryptPasswordEncoder bCryptPasswordEncoder; // 비밀번호 암호화

    @Autowired
    private UserRepository userRepository; // 사용자 저장소

    @Autowired
    private HttpSession session; // HTTP 세션 객체

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 OAuth2UserService를 사용해 사용자 정보를 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 구글 사용자 정보 추출
        String googleId = oAuth2User.getAttribute("sub"); // 구글 고유 ID
        String username = oAuth2User.getAttribute("name"); // 사용자 이름
        String email = oAuth2User.getAttribute("email"); // 사용자 이메일
        String password = bCryptPasswordEncoder.encode("defaultPassword"); // 기본 비밀번호 설정
        String role = "ROLE_USER"; // 기본 역할 설정

        // 중복되지 않는 username 생성
        username = generateUniqueUsername(username);

        // DB에 사용자 정보가 있는지 확인
        User userEntity = userRepository.findByEmail(email).orElse(null);

        if (userEntity == null) {
            // 사용자 정보가 없으면 새 사용자 등록
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .isGoogleUser(true) // 구글 사용자 여부 설정
                    .googleId(googleId) // 구글 ID 설정
                    .name(username) // 이름 설정
                    .build();

            // 새 사용자 정보 저장
            try {
                userRepository.save(userEntity);
            } catch (DataIntegrityViolationException e) {
                // 중복으로 인한 예외 처리
                OAuth2Error oauth2Error = new OAuth2Error("user_registration_error", "User registration failed due to duplicate value", null);
                throw new OAuth2AuthenticationException(oauth2Error, e);
            }
        } else {
            // 이미 등록된 구글 사용자인 경우
            System.out.println("Returning Google login user.");
        }

        // 세션에 사용자 정보 저장
        session.setAttribute("user", userEntity);
        session.setAttribute("isGoogleUser", true);
        session.setAttribute("name", username); // 구글 사용자 이름 저장

        // 사용자 정보를 포함한 PrincipalDetails 객체 반환
        return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
    }

    /**
     * 중복되지 않는 username을 생성하는 메서드
     */
    private String generateUniqueUsername(String username) {
        int suffix = 1;
        String originalUsername = username; // 원래의 사용자 이름 저장
        while (userRepository.findByUsername(username).isPresent()) {
            // 중복된 이름이 있을 경우 숫자 접미사를 붙임
            username = originalUsername + suffix;
            suffix++;
        }
        return username;
    }
}
