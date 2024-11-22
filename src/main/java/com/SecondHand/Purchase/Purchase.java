package com.SecondHand.Purchase;

import com.SecondHand.item.Item;
import com.SecondHand.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 고유 id, 자동 생성되는 기본 키

    @ManyToOne(fetch = FetchType.LAZY) // 구매자 정보는 필요할 때 로딩 (지연 로딩)
    @JoinColumn(name = "user_id") // 'user_id' 컬럼과 연결
    @JsonIgnore // JSON 직렬화 시 해당 필드 제외
    private User buyer; // 구매자 정보

    @ManyToOne // 구매한 물품 정보 (기본 EAGER 로딩)
    @JoinColumn(name = "item_id") // 'item_id' 컬럼과 연결
    @JsonIgnore // JSON 직렬화 시 해당 필드 제외
    private Item item; // 구매한 물품

    private LocalDateTime purchasedDate; // 구매일, 구매가 완료된 시점
}
