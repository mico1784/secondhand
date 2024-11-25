package com.SecondHand.review;

import com.SecondHand.item.Item;
import com.SecondHand.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "reviews") // 테이블 이름 설정
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 리뷰 ID

    @Column(nullable = false)
    private String content; // 리뷰 내용

    @Column(nullable = false)
    private int rating; // 평점 (1~5)

    @ManyToOne(fetch = FetchType.LAZY) // 아이템과의 다대일 관계 설정, 지연 로딩
    @JsonIgnore // JSON 직렬화 시 필드 무시
    @JoinColumn(name = "item_id", nullable = false) // 외래 키 설정
    private Item boughtItem; // 구매된 아이템

    @ManyToOne(fetch = FetchType.LAZY) // 리뷰어(사용자)와의 다대일 관계 설정, 지연 로딩
    @JsonIgnore // JSON 직렬화 시 필드 무시
    @JoinColumn(name = "user_id", nullable = false) // 외래 키 설정
    private User reviewer; // 리뷰어 정보

    @Column(nullable = false)
    private LocalDateTime createdAt; // 작성일

    private String profileImageURL;

    // 아이템 설정 메서드 (양방향 관계 설정을 위해)
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
                ", itemId=" + (boughtItem != null ? boughtItem.getId() : null) + // 아이템 ID 출력
                ", userId=" + (reviewer != null ? reviewer.getId() : null) + // 리뷰어 ID 출력
                '}';
    }
}
