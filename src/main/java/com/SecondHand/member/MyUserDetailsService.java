package com.SecondHand.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // DB에서 username을 가진 유저를 찾아서 불러오기
        Optional<User> result = userRepository.findByUsername(username);

        if (result.isEmpty()) {
            throw new UsernameNotFoundException("아이디가 존재하지 않습니다.");
        }

        User user = result.get();

        // 권한 리스트 초기화
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // CustomUser 생성 시, null 방지를 위한 password 기본값 처리
        String password = user.getPassword() != null ? user.getPassword() : "";

        // 카카오 사용자 여부와 ID 설정
        Long kakaoId = user.getKakaoId(); // User 엔터티에 kakaoId 필드가 있다고 가정
        boolean isKakaoUser = kakaoId != null;

        // CustomUser 객체 반환
        CustomUser customUser;
        if (isKakaoUser) {
            customUser = new CustomUser(user.getUsername(), password, authorities, kakaoId);
        } else {
            customUser = new CustomUser(user.getUsername(), password, authorities);
        }

        customUser.username = user.getUsername();
        customUser.id = user.getId();
        return customUser;
    }
}
