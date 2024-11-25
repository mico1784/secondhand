package com.SecondHand.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

// CustomUser 클래스는 Spring Security의 User 클래스를 확장하여 사용자 정보를 커스터마이징
public class CustomUser extends User {
    private String customUsername; // 사용자 지정 이름
    private Long id; // 사용자 ID
    private Long kakaoId; // 카카오 사용자 ID
    private boolean isKakaoUser; // 카카오 사용자 여부
    private String googleId; // 구글 사용자 ID
    private boolean isGoogleUser; // 구글 사용자 여부

    // 기본 생성자: 일반 사용자 (소셜 미디어 사용자가 아닌 경우)
    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.isKakaoUser = false;
        this.isGoogleUser = false;
    }

    // 카카오 사용자를 위한 생성자
    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities, Long kakaoId) {
        super(username, password, authorities);
        this.kakaoId = kakaoId;
        this.isKakaoUser = true;
        this.isGoogleUser = false;
    }

    // 구글 사용자를 위한 생성자
    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities, String googleId) {
        super(username, password, authorities);
        this.googleId = googleId;
        this.isKakaoUser = false;
        this.isGoogleUser = true;
    }

    // 사용자 지정 이름 설정 메서드
    public void setCustomUsername(String customUsername) {
        this.customUsername = customUsername;
    }

    // 사용자 ID 설정 메서드
    public void setId(Long id) {
        this.id = id;
    }

    // 카카오 ID 설정 메서드
    public void setKakaoId(Long kakaoId) {
        this.kakaoId = kakaoId;
    }

    // 사용자 지정 이름 반환 메서드
    public String getCustomUsername() {
        return customUsername;
    }

    // 사용자 ID 반환 메서드
    public Long getId() {
        return id;
    }

    // 카카오 ID 반환 메서드
    public Long getKakaoId() {
        return kakaoId;
    }

    // 카카오 사용자 여부 반환 메서드
    public boolean isKakaoUser() {
        return isKakaoUser;
    }

    // 구글 ID 반환 메서드
    public String getGoogleId() {
        return googleId;
    }

    // 구글 사용자 여부 반환 메서드
    public boolean isGoogleUser() {
        return isGoogleUser;
    }
}
