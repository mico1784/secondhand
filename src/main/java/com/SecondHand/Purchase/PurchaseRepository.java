package com.SecondHand.Purchase;

import com.SecondHand.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    // 특정 구매자를 기준으로 구매 내역을 구매 날짜 내림차순으로 조회
    List<Purchase> findByBuyerOrderByPurchasedDateDesc(User user);

    // 특정 구매자의 모든 구매 기록 삭제
    void deleteByBuyer(User buyer);

    // 특정 아이템 ID에 대해 구매 정보와 관련된 구매자 정보를 함께 가져오는 쿼리
    @Query("SELECT p FROM Purchase p LEFT JOIN FETCH p.buyer WHERE p.item.id = :itemId")
    List<Purchase> findPurchasesWithBuyerByItemId(@Param("itemId") Long itemId);
}
