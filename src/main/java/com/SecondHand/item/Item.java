package com.SecondHand.item;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

    @CreationTimestamp
    private LocalDateTime uploadDate;   //등록 날짜




}
