package com.SecondHand.wishList;

import com.SecondHand.member.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class WishList {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // 고유 ID


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // User의 id를 참조

    // 디테일에서 뽑아 올 내용
    private Long itemId;            // 제품ID
    private String itemTitle;       // 제품명
    private String itemImgURL;      // 제품 이미지
    private Integer itemPrice;      // 제품 가격
    private LocalDateTime itemUploadedDate; // 제품 등록일
}
