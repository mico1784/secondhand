package com.SecondHand.item;

import com.SecondHand.Purchase.Purchase;
import com.SecondHand.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import com.SecondHand.review.Review;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
@Data
public class Item {

    // 상품 상태를 정의하는 열거형
    public enum ItemSituation {
        판매중, 예약중, 판매완료
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 고유 ID

    private String title; // 상품명
    private Integer price; // 가격
    private String imgURL; // 이미지 URL
    private String itemDesc; // 상품 설명
    private String category; // 카테고리
    private String subcategory; // 서브 카테고리
    private String location; // 거래 지역

    @Enumerated(EnumType.STRING)
    private ItemSituation situation = ItemSituation.판매중; // 상품 상태, 기본값은 '판매중'

    @CreationTimestamp
    private LocalDateTime uploadDate; // 등록 날짜 자동 생성

    // 상품과 연관된 리뷰 목록, 즉시 로딩(FetchType.EAGER)으로 설정
    @OneToMany(mappedBy = "boughtItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Review> reviews = new ArrayList<>(); // 리뷰 리스트 초기화

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore // JSON 응답에 포함되지 않도록 설정
    private User seller; // 판매자 정보

    @Override
    public String toString() {
        // 아이템의 핵심 정보만 포함한 toString 메서드
        return "Item{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", imgURL='" + imgURL + '\'' +
                '}';
    }

    // 구매된 내역을 저장하는 목록
    @OneToMany(mappedBy = "item")
    private List<Purchase> purchases; // 구매 내역

    // 상태 업데이트 메서드
    public void updateSituation(ItemSituation newSituation) {
        this.situation = newSituation; // 새로운 상태로 변경
    }

    // 리뷰 추가 메서드
    public void addReview(Review review) {
        if (reviews != null) {
            reviews.add(review);
            review.setBoughtItem(this); // Item과 Review 간의 관계 설정
        }
    }

    // 리뷰 제거 메서드
    public void removeReview(Review review) {
        if (reviews != null) {
            reviews.remove(review);
            review.setBoughtItem(null); // Item과 Review 간의 관계 해제
        }
    }

    @Transient
    private String formattedPrice; // 천 단위 구분 기호가 포함된 가격 (데이터베이스에 저장되지 않음)

    // 가격을 천 단위로 포맷팅하여 반환하는 메서드
    public String getFormattedPrice() {
        if (this.price != null) {
            NumberFormat formatter = NumberFormat.getInstance(Locale.KOREA); // 한국 기준으로 포맷팅
            return formatter.format(this.price);
        }
        return null; // 가격이 없을 경우 null 반환
    }

    @Transient // 데이터베이스에 매핑되지 않는 필드
    private String readableUploadTime; // 사람이 읽기 쉬운 업로드 시간

    // 업로드 시간을 사람이 읽기 쉬운 형식으로 반환하는 메서드
    public String getReadableUploadTime() {
        if (this.uploadDate == null) {
            return "알 수 없음"; // 업로드 날짜가 없을 경우
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(this.uploadDate, now);

        // 시간 간격에 따라 반환 형식을 다르게 설정
        if (duration.toDays() > 0) {
            return duration.toDays() + "일 전";
        } else if (duration.toHours() > 0) {
            return duration.toHours() + "시간 전";
        } else if (duration.toMinutes() > 0) {
            return duration.toMinutes() + "분 전";
        } else {
            return "방금 전"; // 1분 이내일 경우
        }
    }
}
