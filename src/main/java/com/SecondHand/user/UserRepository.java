package com.SecondHand.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// User 엔터티를 위한 JPA 리포지토리 인터페이스
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일을 기반으로 사용자 검색
    Optional<User> findByEmail(String email);

    // 사용자 이름을 기반으로 사용자 검색
    Optional<User> findByUsername(String username); // 사용자 이름으로 찾기

    // 특정 아이디가 존재하는지 확인
    boolean existsByUsername(String username); // 중복검사 아이디

    // 특정 이메일이 존재하는지 확인
    boolean existsByEmail(String email); // 중복검사 이메일

    // 특정 휴대폰 번호가 존재하는지 확인
    boolean existsByPhoneNumber(String phoneNumber); // 중복검사 휴대폰번호

    // 특정 Google ID가 존재하는지 확인
    boolean existsByGoogleId(String googleId);

    // 카카오 ID로 사용자 검색
    Optional<User> findByKakaoId(Long kakaoId);

    // Google ID로 사용자 검색
    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByName(String name);
}
