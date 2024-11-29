package com.SecondHand.user;

import com.SecondHand.Purchase.Purchase;
import com.SecondHand.review.Review;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set; // Set 클래스를 임포트
import java.util.List; // List 클래스를 임포트
import com.SecondHand.item.Item; // Item 클래스 임포트

import java.time.LocalDateTime;

@Entity
@Table(name = "user") // 데이터베이스에서 "user" 테이블과 매핑
@Getter
@Setter
@NoArgsConstructor // 기본 생성자 생성
@AllArgsConstructor // 모든 필드를 포함하는 생성자 생성
@Builder // 빌더 패턴 사용 가능
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성되는 기본 키
    private Long id;

    private String name; // 사용자 이름

    @Column(unique = true) // 중복된 값 불허 (고유값)
    private String username; // 사용자 아이디

    private String password; // 사용자 비밀번호

    @Column(unique = true) // 중복된 값 불허 (고유값)
    private String email; // 사용자 이메일

    private int age; // 사용자 나이

    private String role; // 사용자 역할 (ex. ADMIN, USER)

    @Column(unique = true) // 중복된 값 불허 (고유값)
    private String phoneNumber; // 사용자 전화번호

    private String gender; // 사용자 성별

    private String address; // 사용자 주소

    private LocalDateTime createdAt; // 사용자 계정 생성 시간

    private String profileImageURL;

    @Column(nullable = false)
    @Builder.Default
    private Integer trustGuage = 50; // 신뢰도 게이지, 기본값은 50

    @OneToMany(mappedBy = "seller") // User와 Item 간의 관계 설정 (1:N)
    private Set<Item> items; // 사용자가 판매 중인 아이템들

    @OneToMany(mappedBy = "buyer") // User와 Purchase 간의 관계 설정 (1:N)
    private List<Purchase> purchases; // 사용자가 구매한 물품 리스트

    @OneToMany(mappedBy = "reviewer") // User와 Review 간의 관계 설정 (1:N)
    private List<Review> reviews; // 사용자가 작성한 리뷰 리스트

    @Column(unique = true)
    private Long kakaoId; // 카카오 사용자 ID (고유)

    private boolean isKakaoUser = false; // 카카오 사용자 여부

    @Column(unique = true)
    private String googleId; // 구글 사용자 ID (고유)

    private boolean isGoogleUser = false; // 구글 사용자 여부

    @Column(name = "withdrawal_scheduled_at") // 탈퇴 예정 시간 필드
    private LocalDateTime withdrawalScheduledAt; // 사용자 탈퇴 예정 시간

    @Column(name = "withdrawal_date") // 실제 탈퇴 시간 필드
    private LocalDateTime withdrawalDate; // 사용자 탈퇴 시간

    @Column(nullable = false)
    @Builder.Default
    private Boolean isWithdrawn = false; // 탈퇴 여부, 기본값은 false

    @PrePersist // 엔티티가 persist 되기 전 실행
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now(); // 생성 시간 자동 설정
        }
    }

    @Override
    public String toString() {
        // 중요 정보 보호를 위해 필요한 필드만 반환
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
