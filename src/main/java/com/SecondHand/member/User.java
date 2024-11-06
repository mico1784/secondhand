package com.SecondHand.member;

import com.SecondHand.item.Item;
import com.SecondHand.review.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 이름

    @Column(unique = true)
    private String username; // 아이디

    private String password; // 비밀번호

    private String profileImgURL;   // 프사

    @Column(unique = true)
    private String email; // 이메일

    private int age; // 나이

    private String role;

    @Column(unique = true)
    private String phoneNumber; // 전화번호

    private String gender; // 성별

    private String address; // 주소

    private LocalDateTime createdAt; // 가입 날짜

    @Column(nullable = false)
    private Integer trustGuage = 50; // 신뢰지수

    @OneToMany(mappedBy = "seller")
    private Set<Item> items; // 판매 물품

    @OneToMany(mappedBy = "reviewer")
    private List<Review> reviews;

    // 카카오 ID 필드 추가 (카카오 사용자 식별용)
    @Column(unique = true)
    private Long kakaoId;

    // 카카오 사용자 여부 확인 필드
    private boolean isKakaoUser = false;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
