package com.SecondHand.review;

import com.SecondHand.item.Item;
import com.SecondHand.item.ItemService;
import com.SecondHand.user.User;
import com.SecondHand.user.UserRepository;
import com.SecondHand.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService; // 리뷰 관련 서비스 의존성 주입

    @Autowired
    private ItemService itemService; // 아이템 관련 서비스 의존성 주입

    @Autowired
    private UserService userService; // 사용자 관련 서비스 의존성 주입

    @Autowired
    private UserRepository userRepository; // 사용자 리포지토리 의존성 주입

    // 리뷰 작성 처리
    @PostMapping("/item/review")
    public String createReview(@RequestParam Long itemId,
                               @RequestParam String content,
                               @RequestParam int rating,
                               Principal principal) {
        // 사용자가 로그인되지 않은 경우 로그인 페이지로 리다이렉트
        if (principal == null) {
            return "redirect:/login?redirect=/item/" + itemId; // 로그인 후 원래 페이지로 돌아오기 위한 리다이렉트 설정
        }

        // 현재 로그인한 사용자의 username을 통해 User 객체를 찾음
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다.")); // 사용자 정보가 없을 경우 예외 처리

        // 아이템 찾기
        Item item = itemService.getItemById(itemId);
        if (item == null) {
            return "redirect:/error"; // 아이템이 없으면 에러 페이지로 리다이렉트
        }

        // 리뷰 객체 생성 및 설정
        Review review = new Review();
        review.setContent(content); // 리뷰 내용 설정
        review.setRating(rating); // 평점 설정
        review.setCreatedAt(LocalDateTime.now()); // 작성일 설정
        review.setBoughtItem(item); // 리뷰 대상 아이템 설정
        review.setReviewer(user); // 로그인한 사용자를 리뷰 작성자로 설정

        // 리뷰 저장
        reviewService.saveReview(review);

        // 리뷰 작성 후 아이템 상세 페이지로 리다이렉트
        return "redirect:/item/" + itemId;
    }

    // 리뷰 작성 폼 보여주기
    @GetMapping("/item/review/{id}")
    public String showReviewForm(@PathVariable Long id, Model model, Principal principal, HttpServletResponse response) throws IOException {
        // 사용자가 로그인되지 않은 경우 로그인 페이지로 리다이렉트
        if (principal == null) {
            response.sendRedirect("/login?redirect=/item/review/" + id); // 로그인 후 원래 페이지로 돌아오기 위한 리다이렉트 설정
            return null;
        }

        // 아이템 가져오기
        Item item = itemService.getItemById(id);
        if (item == null) {
            model.addAttribute("error", "아이템을 찾을 수 없습니다."); // 아이템이 없을 경우 오류 메시지 설정
            return "error"; // 오류 페이지로 이동
        }
        model.addAttribute("item", item); // 모델에 아이템 정보 추가

        // 현재 로그인한 사용자의 정보 가져오기
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다.")); // 사용자 정보가 없을 경우 예외 처리
        model.addAttribute("user", user); // 모델에 사용자 정보 추가

        return "review"; // 리뷰 작성 페이지로 이동
    }
}
