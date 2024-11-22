package com.SecondHand.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository; // ReviewRepository 주입

    // 리뷰 저장 메서드
    public void saveReview(Review review) {
        reviewRepository.save(review); // 데이터베이스에 리뷰 저장
    }

    // 특정 판매자에게 달린 모든 리뷰를 가져오는 메서드
    public List<ReviewDTO> getReviewsBySellerId(Long sellerId) {
        // 판매자가 올린 상품에 대한 모든 리뷰를 가져옴
        List<Review> reviews = reviewRepository.findByBoughtItem_SellerIdOrderByCreatedAtDesc(sellerId);

        // 각 Review 객체를 ReviewDTO 객체로 변환하여 반환
        return reviews.stream()
                .map(ReviewDTO::new) // Review -> ReviewDTO 변환
                .collect(Collectors.toList()); // 리스트로 수집
    }
}
