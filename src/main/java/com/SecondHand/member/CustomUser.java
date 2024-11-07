package com.SecondHand.member;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUser extends User {
    private String customUsername;
    private Long id;
    private Long kakaoId;
    private boolean isKakaoUser;
    private String googleId;
    private boolean isGoogleUser;

    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.isKakaoUser = false;
        this.isGoogleUser = false;
    }

    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities, Long kakaoId) {
        super(username, password, authorities);
        this.kakaoId = kakaoId;
        this.isKakaoUser = true;
        this.isGoogleUser = false;
    }

    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities, String googleId) {
        super(username, password, authorities);
        this.googleId = googleId;
        this.isKakaoUser = false;
        this.isGoogleUser = true;
    }

    public void setCustomUsername(String customUsername) {
        this.customUsername = customUsername;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomUsername() {
        return customUsername;
    }

    public Long getId() {
        return id;
    }

    public Long getKakaoId() {
        return kakaoId;
    }

    public boolean isKakaoUser() {
        return isKakaoUser;
    }

    public String getGoogleId() {
        return googleId;
    }

    public boolean isGoogleUser() {
        return isGoogleUser;
    }
}
