package com.SecondHand.Purchase;

import com.SecondHand.member.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByBuyerOrderByPurchasedDateDesc(User user);
}
