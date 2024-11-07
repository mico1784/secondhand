package com.SecondHand.member;

import com.SecondHand.member.UserRepository;
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

@Primary
@RequiredArgsConstructor
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Attempting to load user by username: " + username); // 디버그용 로그 추가

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다."));

        System.out.println("Loaded user from database: " + user); // 디버그용 로그 추가

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        String password = user.getPassword() != null ? user.getPassword() : "";

        Long kakaoId = user.getKakaoId();
        String googleId = user.getGoogleId();
        boolean isKakaoUser = kakaoId != null;
        boolean isGoogleUser = googleId != null;

        CustomUser customUser;
        if (isKakaoUser) {
            customUser = new CustomUser(user.getUsername(), password, authorities, kakaoId);
        } else if (isGoogleUser) {
            customUser = new CustomUser(user.getUsername(), password, authorities, googleId);
        } else {
            customUser = new CustomUser(user.getUsername(), password, authorities);
        }

        customUser.setCustomUsername(user.getUsername());
        customUser.setId(user.getId());
        return customUser;
    }
}
