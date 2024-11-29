package com.SecondHand.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Primary // 이 서비스가 기본 UserDetailsService로 사용되도록 설정
@RequiredArgsConstructor // 생성자 주입을 위한 롬복 애너테이션
@Service // Spring 서비스 계층으로 등록
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // 사용자 정보를 검색하기 위한 UserRepository 의존성

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 사용자 이름으로 로그인을 시도할 때 콘솔에 출력 (디버깅용)
        System.out.println("Attempting to load user by username: " + username);

        // 사용자 이름으로 사용자 정보를 데이터베이스에서 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다.")); // 사용자 정보가 없을 시 예외 발생

        // 로드된 사용자 정보를 콘솔에 출력 (디버깅용)
        System.out.println("Loaded user from database: " + user);

        // 사용자에게 부여할 권한 목록 생성
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 기본 권한 설정

        // 사용자의 비밀번호 설정 (null일 경우 빈 문자열)
        String password = user.getPassword() != null ? user.getPassword() : "";

        // 카카오 및 구글 사용자 확인
        Long kakaoId = user.getKakaoId();
        String googleId = user.getGoogleId();
        boolean isKakaoUser = kakaoId != null; // 카카오 사용자 여부 확인
        boolean isGoogleUser = googleId != null; // 구글 사용자 여부 확인

        // 사용자 유형에 따라 CustomUser 객체 생성
        CustomUser customUser;
        if (isKakaoUser) {
            // 카카오 사용자인 경우
            customUser = new CustomUser(user.getUsername(), password, authorities, kakaoId);
        } else if (isGoogleUser) {
            // 구글 사용자인 경우
            customUser = new CustomUser(user.getUsername(), password, authorities, googleId);
        } else {
            // 일반 사용자인 경우
            customUser = new CustomUser(user.getUsername(), password, authorities);
        }

        // CustomUser 객체에 추가 정보 설정
        customUser.setCustomUsername(user.getUsername());
        customUser.setId(user.getId());
        return customUser; // UserDetails 객체 반환
    }
}
