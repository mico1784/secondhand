package com.SecondHand.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    // 사용자 정보를 저장하는 메소드
    public void saveUser(String displayname, String username, String email, String password,
                         String phoneNumber, String address, String gender, int age) {
        // 비밀번호 해싱
        String encodedPassword = passwordEncoder.encode(password);

        // User 객체 생성 및 값 설정
        User user = new User();
        user.setDisplayname(displayname);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        user.setGender(gender);
        user.setAge(age);
        user.setCreatedAt(LocalDateTime.now()); // 현재 시간으로 가입 날짜 설정

        // 사용자 정보 저장
        userRepository.save(user);
    }

    // 사용자 인증 메소드
    public boolean authenticateUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);

        // 사용자 없음 또는 비밀번호 불일치
        // BCryptPasswordEncoder를 사용하여 비밀번호 확인
        return user.filter(value -> passwordEncoder.matches(password, value.getPassword())).isPresent();

    }
}