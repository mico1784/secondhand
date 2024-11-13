package com.SecondHand.item;

import com.SecondHand.Purchase.Purchase;
import com.SecondHand.chat.room.Room;
import com.SecondHand.member.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import com.SecondHand.review.Review;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Item {

    public enum ItemSituation {
        판매중, 예약중, 판매완료
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 고유ID
    private String title;   // 상품명
    private Integer price;  // 가격
    private String imgURL;  // 이미지
    private String itemDesc;    // 설명
    private String category;    // 카테고리
    private String subcategory;  // 서브 카테고리

    @Enumerated(EnumType.STRING)
    private ItemSituation situation = ItemSituation.판매중;   // 물품 현황

    @CreationTimestamp
    private LocalDateTime uploadDate;   // 등록 날짜

    @OneToMany(mappedBy = "boughtItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>(); // 리스트 초기화

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User seller;    // 판매자 정보

    @OneToMany(mappedBy = "item")
    private List<Purchase> purchases;   // 구매된 목록

    @OneToMany(mappedBy = "itemC")
    private List<Room> rooms;   // 상품에 대한 채팅방

    // 상태 업데이트 메서드
    public void updateSituation(ItemSituation newSituation) {
        this.situation = newSituation;
    }

    public void addReview(Review review) {
        if (reviews != null) {
            reviews.add(review);
            review.setBoughtItem(this);  // Item과 Review 연결
        }
    }

    // 리뷰 제거 메서드
    public void removeReview(Review review) {
        if (reviews != null) {
            reviews.remove(review);
            review.setBoughtItem(null);  // Item과 Review 연결 해제
        }
    }

    @Transient
    private String formattedPrice; // 천 단위 구분 기호가 포함된 가격 (가상 필드)

    public String getFormattedPrice() {
        return formattedPrice;
    }

    public void setFormattedPrice(String formattedPrice) {
        this.formattedPrice = formattedPrice;
    }
}