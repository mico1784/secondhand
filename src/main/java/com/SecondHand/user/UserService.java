package com.SecondHand.user;

import com.SecondHand.Purchase.PurchaseRepository;
import com.SecondHand.item.ItemRepository;
import com.SecondHand.review.ReviewRepository;
import com.SecondHand.wishList.WishListRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 새로운 사용자 저장 메서드
    public void saveUser(String name, String username, String email, String password,
                         String phoneNumber, String address, String gender, int age) {

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 비밀번호 및 입력 데이터에 대한 유효성 검사
        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("비밀번호에는 최소 하나의 대문자가 포함되어야 합니다.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("비밀번호에는 최소 하나의 소문자가 포함되어야 합니다.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("비밀번호에는 최소 하나의 숫자가 포함되어야 합니다.");
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("올바른 이메일 형식을 입력해 주세요.");
        }
        if (!phoneNumber.matches("^010\\d{7,8}$")) {
            throw new IllegalArgumentException("전화번호는 010으로 시작하고 숫자 10자리 또는 11자리여야 합니다.");
        }

        // 사용자 중복 검사
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
        }

        // 사용자 생성 및 저장
        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword); // 암호화된 비밀번호 저장
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        user.setGender(gender);
        user.setAge(age);

        userRepository.save(user);
    }

    // 사용자 인증 메서드
    public boolean authenticateUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.filter(value -> passwordEncoder.matches(password, value.getPassword())).isPresent();
    }

    // 카카오 사용자 저장 또는 업데이트 메서드
    public User saveOrUpdateKakaoUser(Long kakaoId, String nickname) {
        Optional<User> existingUser = userRepository.findByUsername("kakao_" + kakaoId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setName(nickname);
            userRepository.save(user); // 업데이트된 사용자 정보 저장
            return user;
        } else {
            // 새로운 카카오 사용자 생성 및 저장
            User newUser = new User();
            newUser.setUsername("kakao_" + kakaoId); // 고유한 카카오 ID를 username으로 설정
            newUser.setName(nickname);
            newUser.setEmail(generateRandomPhoneNumber()+ "@kakao.com");
            newUser.setPassword(""); // 소셜 로그인 사용자는 비밀번호를 비워둠
            newUser.setPhoneNumber(generateRandomPhoneNumber());
            newUser.setGender("");
            newUser.setAddress("");
            newUser.setCreatedAt(LocalDateTime.now());
            userRepository.save(newUser);
            return newUser;
        }
    }

    // 사용자 ID로 조회 메서드
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    // 비밀번호 인코딩 메서드
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }


    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private WishListRepository wishListRepository;

    // 회원 탈퇴 완료 메서드
    @PostMapping("/completeDeleteAccount")
    @Transactional
    public String completeDeleteAccount(Principal principal, HttpSession session, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 후에 회원탈퇴를 진행할 수 있습니다.");
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        try {
            // 탈퇴 예정 시간이 지나면 실제 탈퇴 처리
            if (user.getWithdrawalScheduledAt() != null && user.getWithdrawalScheduledAt().isBefore(LocalDateTime.now())) {
                user.setWithdrawalDate(LocalDateTime.now());
                userRepository.save(user);

                // 사용자 관련 데이터 삭제
                deleteRelatedData(user);

                // 사용자 삭제 및 세션 종료
                userRepository.delete(user);
                session.invalidate();

                redirectAttributes.addFlashAttribute("message", "회원탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "탈퇴 예정 시간이 지나지 않았습니다.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원탈퇴 처리 중 오류가 발생했습니다.");
        }

        return "redirect:/login"; // 탈퇴 후 로그인 페이지로 리다이렉트
    }

    // 사용자 관련 데이터 삭제 메서드
    private void deleteRelatedData(User user) {
        // 사용자가 작성한 리뷰 삭제
        reviewRepository.deleteByReviewer(user);

        // 사용자가 구매한 기록 삭제
        purchaseRepository.deleteByBuyer(user);

        // 사용자가 찜한 목록 삭제
        wishListRepository.deleteByUser(user);

        // 사용자가 판매한 아이템 삭제
        itemRepository.deleteBySeller(user);
    }
    private String generateRandomPhoneNumber() {
        Random random = new Random();
        StringBuilder phoneNumber = new StringBuilder("0"); // 첫 번째 자리는 0으로 고정

        // 나머지 10자리는 랜덤 숫자
        for (int i = 0; i < 10; i++) {
            phoneNumber.append(random.nextInt(10)); // 0~9 사이의 숫자 추가
        }

        return phoneNumber.toString();
    }
}
