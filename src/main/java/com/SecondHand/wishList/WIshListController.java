package com.SecondHand.wishList;

import com.SecondHand.member.CustomUser;
import com.SecondHand.member.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.List;
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
                   Authentication auth){
        if(auth == null || !auth.isAuthenticated()){
            return "login.html";
        }
        CustomUser user = (CustomUser)auth.getPrincipal();
        var userdata = userRepository.findByUsername(user.getUsername());
        Optional<WishList> existingItem = wishListRepository.findByItemIdAndUser(itemId, userdata.get());

        if (existingItem.isPresent()) {
            wishListRepository.delete(existingItem.get());
            // 찜 목록에서 삭제한 경우, 상품 상세 페이지로 리다이렉트
            return "redirect:/item/" + itemId;
        } else {
            wIshListService.saveWishList(itemId, itemTitle, itemImgURL, itemPrice, itemUploadedDate, auth);
            // 찜하기 완료 후 상품 상세 페이지로 리다이렉트
            return "redirect:/item/" + itemId;
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFromList(@RequestParam Long id) {
        wishListRepository.deleteById(id);
        return ResponseEntity.ok("삭제 완료");
    }

}
