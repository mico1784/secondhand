package com.SecondHand.review;

import com.SecondHand.item.Item;
import com.SecondHand.member.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 리뷰 ID

    @Column(nullable = false)
    private String content; // 리뷰 내용

    @Column(nullable = false)
    private int rating; // 평점 (1~5)

    @ManyToOne(fetch = FetchType.LAZY) @JsonIgnore
    @JoinColumn(name = "item_id", nullable = false)
    private Item boughtItem; // 아이템과의 관계

    @ManyToOne(fetch = FetchType.LAZY) @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private User reviewer; // 사용자와의 관계

    @Column(nullable = false)
    private LocalDateTime createdAt; // 작성일

    public void setBoughtItem(Item boughtItem) {
        this.boughtItem = boughtItem;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", rating=" + rating +
                ", createdAt=" + createdAt +
                ", itemId=" + (boughtItem != null ? boughtItem.getId() : null) +
                ", userId=" + (reviewer != null ? reviewer.getId() : null) +
                '}';
    }
}
