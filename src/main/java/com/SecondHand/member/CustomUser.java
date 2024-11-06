package com.SecondHand.member;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUser extends User {
    public String username;
    public Long id;
    public Long kakaoId; // 카카오 사용자 식별용 ID
    public boolean isKakaoUser; // 카카오 사용자 여부 확인용 필드

    public CustomUser(String username,
                      String password,
                      Collection<? extends GrantedAuthority> authorities
    ) {
        super(username, password, authorities);
        this.isKakaoUser = false; // 기본값: 일반 사용자
    }

    // 카카오 사용자용 생성자
    public CustomUser(String username,
                      String password,
                      Collection<? extends GrantedAuthority> authorities,
                      Long kakaoId
    ) {
        super(username, password, authorities);
        this.kakaoId = kakaoId;
        this.isKakaoUser = true; // 카카오 사용자로 설정
    }
}
