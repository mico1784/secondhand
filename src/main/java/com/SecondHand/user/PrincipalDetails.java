package com.SecondHand.user;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Data // 롬복을 이용해 getter, setter, toString 등을 자동 생성
public class PrincipalDetails implements UserDetails, OAuth2User {

    private final User user; // 사용자 정보를 담고 있는 객체
    private Map<String, Object> attributes; // OAuth2 사용자 속성 정보

    // 기존 User 객체만 받는 생성자 (일반 로그인 시 사용)
    public PrincipalDetails(User user) {
        this.user = user;
    }

    // User 객체와 OAuth2 속성 정보를 함께 받는 생성자 (OAuth2 로그인 시 사용)
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // OAuth2User 인터페이스 메서드: OAuth2 속성 정보를 반환
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // OAuth2User 인터페이스 메서드: 사용자 이름 반환
    @Override
    public String getName() {
        return user.getUsername(); // 사용자 객체에서 username 반환
    }

    // UserDetails 인터페이스 메서드: 사용자 권한 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // 권한 로직 필요 시 구현
    }

    // UserDetails 인터페이스 메서드: 사용자 비밀번호 반환
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // UserDetails 인터페이스 메서드: 사용자 이름 반환
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // 계정 만료 여부 반환 (true면 만료되지 않음)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 여부 반환 (true면 잠기지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 자격 증명 만료 여부 반환 (true면 만료되지 않음)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부 반환 (true면 활성화 상태)
    @Override
    public boolean isEnabled() {
        return true;
    }
}
