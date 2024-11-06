package com.SecondHand.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 일반 사용자 저장 메서드
    public void saveUser(String name, String username, String email, String password,
                         String phoneNumber, String address, String gender, int age) {
        String encodedPassword = passwordEncoder.encode(password);

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
        }

        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        user.setGender(gender);
        user.setAge(age);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    // 사용자 인증 메소드
    public boolean authenticateUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.filter(value -> passwordEncoder.matches(password, value.getPassword())).isPresent();
    }

    // 카카오 사용자 저장 또는 업데이트 메소드
    public User saveOrUpdateKakaoUser(Long kakaoId, String nickname) {
        Optional<User> existingUser = userRepository.findByUsername("kakao_" + kakaoId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setName(nickname);
            userRepository.save(user); // 업데이트된 사용자 정보 저장
            return user;
        } else {
            User newUser = new User();
            newUser.setUsername("kakao_" + kakaoId); // 고유한 카카오 ID를 username으로 설정
            newUser.setName(nickname);
            newUser.setPassword(""); // 소셜 로그인 사용자는 비밀번호를 비워둠
            newUser.setCreatedAt(LocalDateTime.now());
            userRepository.save(newUser);
            return newUser;
        }
    }

    // 사용자 ID로 조회
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    // 비밀번호 인코딩 메소드
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
