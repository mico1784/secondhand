package com.SecondHand.member;

import com.SecondHand.item.Item;
import com.SecondHand.item.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ItemService itemService;

    @GetMapping("/home")
    public String home(Model model, Principal principal,
                       @RequestParam(required = false, defaultValue = "") String category,
                       @RequestParam(defaultValue = "1") Integer page) {
        // 사용자가 로그인한 상태인지 확인하고 상태에 따른 정보 추가
        boolean isLoggedIn = principal != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            model.addAttribute("username", principal.getName());
        }

        // 아이템 목록을 모델에 추가
        Page<Item> itemList;

        try {
            if (category.isEmpty()) { // 정해진 카테고리가 없으면 전체 목록을 반환
                itemList = itemService.getAllItems(PageRequest.of(page - 1, 3, Sort.by(Sort.Direction.DESC, "id")));
            } else { // 정해진 카테고리가 있다면
                itemList = itemService.getItemsByCategory(category, PageRequest.of(page - 1, 3, Sort.by(Sort.Direction.DESC, "id")));
                model.addAttribute("category", category);
            }

            model.addAttribute("items", itemList.getContent());
            model.addAttribute("hasPrevious", itemList.hasPrevious());
            model.addAttribute("hasNext", itemList.hasNext());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPage", itemList.getTotalPages());

        } catch (Exception e) {
            model.addAttribute("error", "아이템 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            // 기본적으로 빈 리스트를 반환할 수 있습니다.
            model.addAttribute("items", List.of());
            model.addAttribute("totalPage", 0);
        }

        return "index"; // home.html 뷰로 이동
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
            return "redirect:/index"; // 로그인 성공 시 홈으로 리다이렉트
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

    @GetMapping("/my-page")
    public String myPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        model.addAttribute("user", user); // 사용자 정보를 모델에 추가

        // createdAt 필드 포맷팅
        LocalDateTime createdAt = user.getCreatedAt();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = createdAt.format(formatter);
        model.addAttribute("formattedDate", formattedDate); // 포맷된 날짜 추가

        return "my-page"; // mypage.html 파일을 반환
    }

    // 사용자 저장
    @PostMapping("/add")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.saveUser(
                    user.getName(),
                    user.getUsername(), // displayname을 username으로 변경
                    user.getEmail(),
                    user.getPassword(), // 해싱은 UserService에서 처리됩니다.
                    user.getPhoneNumber(),
                    user.getAddress(),
                    user.getGender(),
                    user.getAge()
            );

            // 성공 메시지 설정
            redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다!");

            return "redirect:/login"; // 가입 후 로그인 페이지로 리다이렉트

        } catch (IllegalArgumentException e) {
            // 예외 메시지를 에러 메시지로 설정
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }


    }

}

