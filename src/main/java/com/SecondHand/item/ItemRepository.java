package com.SecondHand.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // 페이징 처리
    Page<Item> findAll(Pageable page);
    Page<Item> findPageByCategory(String category, Pageable page);
    @Query(value="select * from item where title like %:searchText%", nativeQuery = true)
    Page<Item> findPageByTitleContains(@Param("searchText") String searchText, Pageable page);
}
