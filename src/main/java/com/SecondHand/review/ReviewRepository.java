package com.SecondHand.review;

import com.SecondHand.member.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    long countByBoughtItem_Seller(User seller);
    List<Review> findByReviewerOrderByCreatedAtDesc(User reviewer);
    List<Review> findByBoughtItem_SellerOrderByCreatedAtDesc(User seller);
}