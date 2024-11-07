package com.SecondHand.Purchase;

import com.SecondHand.item.Item;
import com.SecondHand.member.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Purchase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 고유 id

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User buyer; // 구매자 정보

    @ManyToOne
    @JoinColumn(name = "item_id")
    @JsonIgnore
    private Item item;  // 구매한 물품

    private LocalDateTime purchasedDate;    // 구매일

}
