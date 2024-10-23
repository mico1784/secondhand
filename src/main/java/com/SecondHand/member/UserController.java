package com.SecondHand.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    // 로그인 페이지를 표시하는 메소드
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
        }
        return "login"; // login.html 파일을 반환
    }

    // 사용자 등록 페이지를 위한 GET 메서드
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        return "register"; // register.html 파일을 반환
    }

    // 사용자 저장
    @PostMapping("/add")
    public String createUser(@ModelAttribute User user) {
        userService.saveUser(
                user.getDisplayname(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),  // 비밀번호 해싱을 하지 않음
                user.getPhoneNumber(),
                user.getAddress(),
                user.getGender(),
                user.getAge()
        );
        return "redirect:/login"; // 가입 후 로그인 페이지로 리다이렉트
    }
}
