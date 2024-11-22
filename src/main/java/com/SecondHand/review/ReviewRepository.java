package com.SecondHand.review;

import com.SecondHand.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    long countByBoughtItem_Seller(User seller);

    List<Review> findByReviewerOrderByCreatedAtDesc(User reviewer);

    List<Review> findByBoughtItem_SellerOrderByCreatedAtDesc(User seller);

    List<Review> findByBoughtItem_SellerIdOrderByCreatedAtDesc(Long sellerId);

    void deleteByReviewer(User reviewer);

    // @EntityGraph를 사용하여 reviewer를 즉시 로딩하도록 설정
    @EntityGraph(attributePaths = {"reviewer"})
    Optional<Review> findReviewById(Long id);

    // @EntityGraph를 사용하여 boughtItem과 관련된 Seller 정보를 즉시 로딩하도록 설정
    @EntityGraph(attributePaths = {"boughtItem.seller"})
    List<Review> findWithSellerByBoughtItemSellerIdOrderByCreatedAtDesc(Long sellerId);

    @Query("SELECT r FROM Review r JOIN FETCH r.boughtItem WHERE r.boughtItem.id = :itemId")
    List<Review> findReviewsWithItemByItemId(@Param("itemId") Long itemId);

}
