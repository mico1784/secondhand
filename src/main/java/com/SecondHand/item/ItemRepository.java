package com.SecondHand.item;

import com.SecondHand.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Item 엔티티에 대한 JPA 레포지토리 인터페이스
public interface ItemRepository extends JpaRepository<Item, Long> {

    // 모든 아이템을 페이징 처리하여 조회
    Page<Item> findAll(Pageable page);

    // 특정 카테고리의 아이템을 페이징 처리하여 조회
    Page<Item> findPageByCategory(String category, Pageable page);

    // 제목에 특정 텍스트를 포함하는 아이템을 페이징 처리하여 조회 (네이티브 쿼리 사용)
    @Query(value="select * from item where title like %:searchText%", nativeQuery = true)
    Page<Item> findPageByTitleContains(@Param("searchText") String searchText, Pageable page);

    // 특정 판매자에 의해 등록된 아이템을 조회
    List<Item> findBySeller(User seller);

    // 특정 판매자에 의해 등록된 특정 상태의 아이템을 조회
    List<Item> findBySellerAndSituation(User seller, Item.ItemSituation situation);

    // 카테고리와 가격 범위에 따라 아이템을 조회 (JPQL 사용)
    @Query("SELECT i FROM Item i WHERE i.category = :category AND i.price BETWEEN :minPrice AND :maxPrice")
    List<Item> findByCategoryAndPriceRange(@Param("category") String category,
                                           @Param("minPrice") Integer minPrice,
                                           @Param("maxPrice") Integer maxPrice);

    // 특정 카테고리의 아이템만 조회
    List<Item> findByCategory(String category, Sort sort);

    Page<Item> findByCategory(String category, Pageable pageable);

    // 특정 판매자가 등록한 아이템을 삭제
    void deleteBySeller(User seller);

    // 최신 게시물 가져오기
    List<Item> findAll(Sort sort);

}
