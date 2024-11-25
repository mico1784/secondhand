package com.SecondHand.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity // JPA 엔티티임을 나타냄
@Getter // Getter 메서드를 자동 생성
@Setter // Setter 메서드를 자동 생성
@NoArgsConstructor // 기본 생성자를 자동 생성
@Table(name = "persistent_logins") // 데이터베이스 테이블 이름 설정
public class PersistentLogin {

    @Column(nullable = false) // NULL을 허용하지 않음
    private String username; // 사용자 이름

    @Id // 주 키를 나타냄
    @Column(length = 64, nullable = false) // 길이와 NULL 허용 여부 설정
    private String series; // 시리즈 토큰

    @Column(length = 64, nullable = false) // 길이와 NULL 허용 여부 설정
    private String token; // 인증 토큰

    @Column(name = "last_used", nullable = false) // 컬럼 이름과 NULL 허용 여부 설정
    private LocalDateTime lastUsed; // 마지막 사용 시각
}
