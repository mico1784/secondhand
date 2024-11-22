package com.SecondHand.Purchase;

import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class PurchaseDTO {
    private Long id; // 구매의 고유 ID
    private Long itemId; // 구매한 아이템의 ID
    private String itemTitle; // 구매한 아이템의 제목
    private String itemImgURL; // 구매한 아이템의 이미지 URL
    private Long UserId; // 구매자의 ID
    private String purchasedDate; // 구매일을 포맷팅한 문자열

    // Purchase 엔티티 객체를 기반으로 DTO 생성자
    public PurchaseDTO(Purchase purchase) {
        this.id = purchase.getId(); // Purchase ID 설정
        this.itemId = purchase.getItem().getId(); // 아이템 ID 설정
        this.itemTitle = purchase.getItem().getTitle(); // 아이템 제목 설정
        this.itemImgURL = purchase.getItem().getImgURL(); // 아이템 이미지 URL 설정
        this.UserId = purchase.getBuyer().getId(); // 구매자 ID 설정
        // 구매일을 'yyyy.MM.dd HH:mm:ss' 형식으로 포맷팅하여 문자열로 저장
        this.purchasedDate = purchase.getPurchasedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
    }
}
