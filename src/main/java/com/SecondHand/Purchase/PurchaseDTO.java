package com.SecondHand.Purchase;

import com.SecondHand.member.User;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class PurchaseDTO {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private String itemImgURL;
    private Long UserId;
    private String purchasedDate;

    public PurchaseDTO(Purchase purchase) {
        this.id = purchase.getId();
        this.itemId = purchase.getItem().getId();
        this.itemTitle = purchase.getItem().getTitle();
        this.itemImgURL = purchase.getItem().getImgURL();
        this.UserId = purchase.getBuyer().getId();
        this.purchasedDate = purchase.getPurchasedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
    }
}
