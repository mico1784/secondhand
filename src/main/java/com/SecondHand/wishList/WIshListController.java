package com.SecondHand.wishList;

import com.SecondHand.user.CustomUser;
import com.SecondHand.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WIshListController {

    private final WishListRepository wishListRepository;
    private final WIshListService wIshListService;
    private final UserRepository userRepository;



    @PostMapping("/add")
    String addList(Long itemId,
                   String itemTitle,
                   String itemImgURL,
                   Integer itemPrice,
                   LocalDateTime itemUploadedDate,
                   Authentication auth) {
        try {
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/login"; // 로그인하지 않았으면 로그인 페이지로 리다이렉트
            }

            if (itemId == null || itemTitle == null || itemImgURL == null || itemPrice == null || itemUploadedDate == null) {
                return "redirect:/item"; // 잘못된 값이 있을 경우 상품 목록 페이지로 리다이렉트
            }

            CustomUser user = (CustomUser) auth.getPrincipal();
            var userdata = userRepository.findByUsername(user.getUsername());
            Optional<WishList> existingItem = wishListRepository.findByItemIdAndUser(itemId, userdata.get());

            if (existingItem.isPresent()) {
                wishListRepository.delete(existingItem.get());
                return "redirect:/item/" + itemId; // 찜 목록에서 삭제 후 상품 상세 페이지로 리다이렉트
            } else {
                wIshListService.saveWishList(itemId, itemTitle, itemImgURL, itemPrice, itemUploadedDate, auth);
                return "redirect:/item/" + itemId; // 찜 목록에 추가 후 상품 상세 페이지로 리다이렉트
            }
        } catch (Exception e) {
            // 오류 발생 시 사용자에게는 아무 메시지나 페이지를 보내지 않고, 콘솔에만 출력
            e.printStackTrace();
            return "redirect:/item"; // 상품 목록 페이지로 리다이렉트
        }
    }



    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFromList(@RequestParam Long id) {
        wishListRepository.deleteById(id);
        return ResponseEntity.ok("삭제 완료");
    }

}
