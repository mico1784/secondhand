package com.SecondHand.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    // 홈 페이지 GET 매핑
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("message", "Welcome to the Home Page!");
        return "home"; // home.html 뷰로 이동
    }

    // 로그인 페이지를 표시하는 메소드
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
            model.addAttribute("username", user.getUsername());
        }

        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
        }
        return "login"; // login.html 파일을 반환
    }

    // 로그인 요청 처리
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) { // displayname을 username으로 변경
        boolean authenticated = userService.authenticateUser(username, password);

        if (authenticated) {
            return "redirect:/home"; // 로그인 성공 시 홈으로 리다이렉트
        } else {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
            return "login"; // 로그인 실패 시 로그인 페이지로 다시 리턴
        }
    }

    // 사용자 등록 페이지를 위한 GET 메서드
    @GetMapping("/register")
    public String showRegistrationForm(Principal principal, Model model) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
            model.addAttribute("username", user.getUsername());
        }
        return "register"; // register.html 파일을 반환
    }

    // 사용자 저장
    @PostMapping("/add")
    public String createUser(@ModelAttribute User user) {
        userService.saveUser(
                user.getUsername(), // displayname을 username으로 변경
                user.getEmail(),
                user.getPassword(), // 해싱은 UserService에서 처리됩니다.
                user.getPhoneNumber(),
                user.getAddress(),
                user.getGender(),
                user.getAge()
        );
        return "redirect:/login"; // 가입 후 로그인 페이지로 리다이렉트
    }
}
