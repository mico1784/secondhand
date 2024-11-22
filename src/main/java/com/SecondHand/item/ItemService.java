package com.SecondHand.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    // 아이템 저장
    @Transactional
    public Item saveItem(Item item) {
        // 판매자 정보가 없는 경우 예외 발생
        if (item.getSeller() == null) {
            throw new IllegalArgumentException("판매자 정보는 필수입니다.");
        }
        // 아이템 저장 후 반환
        return itemRepository.save(item);
    }

    // 모든 아이템 가져오기
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // 페이징된 아이템 리스트 가져오기
    public Page<Item> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    // 기존 Sort 기반 메서드
    public List<Item> getAllItemsByCategorySorted(String category, Sort sort) {
        return itemRepository.findByCategory(category, sort);
    }

    // 새로운 Pageable 기반 메서드
    public Page<Item> getAllItemsByCategoryPaged(String category, Pageable pageable) {
        return itemRepository.findByCategory(category, pageable);
    }

    //최신 게시물 가져오기
    public List<Item> getRecentItems() {
        return itemRepository.findAll(Sort.by(Sort.Direction.DESC, "uploadDate")); // 모든 데이터를 최신순으로 가져옴
    }


    // 아이템 ID로 가져오기 (연관된 판매자 즉시 로딩)
    @EntityGraph(attributePaths = {"seller"})
    public Optional<Item> findItemById(Long id) {
        return itemRepository.findById(id);
    }

    // 아이템 ID로 가져오기 (아이템이 없으면 예외 발생)
    public Item getItemById(Long id) {
        return findItemById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "아이템을 찾을 수 없습니다."));
    }

    // 아이템 삭제
    @Transactional
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    // 아이템 업데이트
    @Transactional
    public Item updateItem(Item updatedItem) {
        // 아이템 ID가 없으면 예외 발생
        if (updatedItem.getId() == null) {
            throw new IllegalArgumentException("아이템 ID는 null일 수 없습니다.");
        }

        // 기존 아이템 가져오기
        Item existingItem = getItemById(updatedItem.getId());

        // 가격이 음수인 경우 예외 발생
        if (updatedItem.getPrice() != null && updatedItem.getPrice() < 0) {
            throw new IllegalArgumentException("가격은 0보다 작을 수 없습니다.");
        }

        // 변경할 필드가 있는 경우 업데이트
        Optional.ofNullable(updatedItem.getTitle()).ifPresent(existingItem::setTitle);
        Optional.ofNullable(updatedItem.getPrice()).ifPresent(existingItem::setPrice);
        Optional.ofNullable(updatedItem.getImgURL()).ifPresent(existingItem::setImgURL);
        Optional.ofNullable(updatedItem.getItemDesc()).ifPresent(existingItem::setItemDesc);
        Optional.ofNullable(updatedItem.getCategory()).ifPresent(existingItem::setCategory);
        Optional.ofNullable(updatedItem.getSubcategory()).ifPresent(existingItem::setSubcategory);
        Optional.ofNullable(updatedItem.getSituation()).ifPresent(existingItem::setSituation);

        // 업데이트된 아이템 저장 후 반환
        return itemRepository.save(existingItem);
    }

    // 아이템 상태 업데이트
    @Transactional
    public void updateItemSituation(Long itemId, Item.ItemSituation situation) {
        updateItemField(itemId, item -> item.updateSituation(situation));
    }

    // 아이템 카테고리 수정
    @Transactional
    public void updateItemCategory(Long itemId, String newCategory) {
        updateItemField(itemId, item -> item.setCategory(newCategory));
    }

    // 공통 필드 업데이트 메서드 (아이템을 받아 특정 필드 업데이트)
    @Transactional
    public void updateItemField(Long itemId, Consumer<Item> updater) {
        Item item = getItemById(itemId);
        updater.accept(item);  // 필드 업데이트
        itemRepository.save(item);  // 저장
    }

    // 수정 권한 체크 (아이템의 판매자와 주어진 사용자 이름 비교)
    public boolean hasEditPermission(Long itemId, String username) {
        Item item = getItemById(itemId);
        return item != null && item.getSeller().getUsername().equals(username);
    }
}
