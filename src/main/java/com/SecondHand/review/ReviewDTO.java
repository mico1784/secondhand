package com.SecondHand.review;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class ReviewDTO {
    private Long id;
    private String content;
    private int rating;
    private Long itemId;  // Item ID만 포함
    private String reviewerUsername;  // Reviewer ID만 포함
    private String createdAt;

    public ReviewDTO(Review review) {
        this.id = review.getId();
        this.content = review.getContent();
        this.rating = review.getRating();
        this.itemId = review.getBoughtItem().getId();
        this.reviewerUsername = review.getReviewer().getUsername();
        this.createdAt = review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
}
