package com.SecondHand.item;

import com.SecondHand.member.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import com.SecondHand.review.Review;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 고유ID
    private String title;   // 상품명
    private Integer price;  // 가격
    private String imgURL;  // 이미지
    private String itemDesc;    // 설명
    private String category;    // 카테고리
    private String subcategory;  // 서브 카테고리
    private String situation = "onSale";   // 물품현황(판매중 / 판매완료)

    @CreationTimestamp
    private LocalDateTime uploadDate;   // 등록 날짜

    @OneToMany(mappedBy = "boughtItem")
    private List<Review> reviews;

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", imgURL='" + imgURL + '\'' +
                // 필요한 필드만 포함
                '}';
    }

    @ManyToOne @JoinColumn(name = "user_id") @JsonIgnore
    private User seller;    // 판매자 정보가 Item에 담김
}
