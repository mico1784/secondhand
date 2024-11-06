package com.SecondHand.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email); // 이메일로 사용자 찾기
    Optional<User> findByUsername(String username); // 사용자 이름으로 찾기
    boolean existsByUsername(String username); // 중복검사 아이디
    boolean existsByEmail(String email); // 중복검사 이메일
    boolean existsByPhoneNumber(String phoneNumber); // 중복검사 휴대폰번호


    Optional<User> findByKakaoId(Long kakaoId);
}
