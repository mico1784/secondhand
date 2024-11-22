package com.SecondHand.wishList;

import com.SecondHand.user.CustomUser;
import com.SecondHand.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WIshListService {

    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;

    public void saveWishList(Long itemId,
                             String itemTitle,
                             String itemImgURL,
                             Integer itemPrice,
                             LocalDateTime itemUploadedDate,
                             Authentication auth){
        WishList wishlist = new WishList();

        CustomUser user = (CustomUser)auth.getPrincipal();
        var userdata = userRepository.findByUsername(user.getUsername());
        wishlist.setUser(userdata.get());

        wishlist.setItemId(itemId);
        wishlist.setItemTitle(itemTitle);
        wishlist.setItemPrice(itemPrice);
        wishlist.setItemImgURL(itemImgURL);
        wishlist.setItemUploadedDate(itemUploadedDate);

        wishListRepository.save(wishlist);
    }

}
