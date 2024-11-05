package com.SecondHand.review;

import com.SecondHand.item.Item;
import com.SecondHand.item.ItemService;
import com.SecondHand.member.User;
import com.SecondHand.member.UserRepository;
import com.SecondHand.member.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/item/review")
    public String createReview(@RequestParam Long itemId, @RequestParam Long userId,
                               @RequestParam String content, @RequestParam int rating) {
        Item item = itemService.getItemById(itemId);
        User user = userService.getUserById(userId);
        System.out.println("Item ID: " + itemId);
        System.out.println("User ID: " + userId);

        // 유효성 검사
        if (item == null) {
            return "redirect:/error"; // 오류 페이지로 리다이렉트
        }

        if (user == null) {
            return "redirect:/error"; // 오류 페이지로 리다이렉트
        }

        Review review = new Review();
        review.setContent(content);
        review.setRating(rating);
        review.setCreatedAt(LocalDateTime.now());
        review.setBoughtItem(item);
        review.setReviewer(user);

        reviewService.saveReview(review); // 리뷰 저장

        return "redirect:/item/" + itemId; // 아이템 상세 페이지로 리다이렉트
    }


    @GetMapping("/item/review/{id}")
    public String showReviewForm(@PathVariable Long id, Model model, Principal principal) {
        Item item = itemService.getItemById(id);
        model.addAttribute("item", item);
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        model.addAttribute("user", user);

        // item 객체를 모델에 추가
        return "review"; // 리뷰 작성 페이지
    }
}
