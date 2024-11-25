package com.SecondHand.review;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class ReviewDTO {
    private Long id; // 리뷰 ID
    private String content; // 리뷰 내용
    private int rating; // 평점
    private Long itemId; // 리뷰가 달린 Item의 ID
    private String reviewerUsername; // 리뷰 작성자의 사용자명 (아이디)
    private String reviewerName; // 리뷰 작성자의 닉네임
    private String createdAt; // 리뷰 작성 날짜 (포맷팅된 문자열)
    private String profileImageURL; // 리뷰 작성자의 프로필 이미지 URL 추가


    // Review 엔티티를 기반으로 ReviewDTO 생성자
    public ReviewDTO(Review review) {
        this.id = review.getId(); // 리뷰 ID 설정
        this.content = review.getContent(); // 리뷰 내용 설정
        this.rating = review.getRating(); // 평점 설정
        this.itemId = review.getBoughtItem().getId(); // Item의 ID 설정
        this.reviewerUsername = review.getReviewer().getUsername(); // 리뷰 작성자의 사용자명 설정
        this.reviewerName = review.getReviewer().getName(); // 리뷰 작성자의 닉네임 설정
        this.createdAt = review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")); // 작성 날짜를 포맷팅하여 설정
        this.profileImageURL = review.getReviewer().getProfileImageURL(); // 리뷰 작성자의 프로필 이미지 URL 설정
    }
}
