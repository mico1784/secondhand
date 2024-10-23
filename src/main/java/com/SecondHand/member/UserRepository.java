package com.SecondHand.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email); // 이메일로 사용자 찾기
    Optional<User> findByUsername(String username);
}
