package com.SecondHand.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    // 목록을 id 내림차순으로 정렬해서 조회(최신 업로드 된 상품 순으로 나옴)
    @Query(value="select * from item order by id desc", nativeQuery = true)
    List<Item> findAllOrderByIdDesc();

    // 카테고리별 목록 id 내림차순 조회
    @Query(value = "select * from item where category = :category order by id desc", nativeQuery = true)
    List<Item> findAllByCategoryOrderByIdDesc(@Param("category") String category);

    // 검색 목록을 id 내림차순으로 정렬해서 조회(최신 업로드 된 상품 순으로 나옴)
    @Query(value="select * from item where match(title) against(?1) order by id desc", nativeQuery = true)
    List<Item> searchByTitle(String title);

    @Query(value="select * from item where title like %:searchText%", nativeQuery = true)
    Page<Item> findPageByTitleContains(@Param("searchText") String searchText, Pageable page);

    // 페이징 처리용
    Page<Item> findAll(Pageable page);
    Page<Item> findPageByCategory(String category, Pageable page);
}
