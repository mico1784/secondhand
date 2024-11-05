package com.SecondHand.wishList;

import com.SecondHand.member.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishListRepository extends JpaRepository<WishList, Long> {
    List<WishList> findByUserOrderByCreatedDateDesc(User user);
    Optional<WishList> findByItemIdAndUser(Long itemId, User user);
}
