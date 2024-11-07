package com.SecondHand.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    // 아이템 저장
    @Transactional
    public Item saveItem(Item item) {
        // 새로운 아이템을 저장할 때 seller와 uploadDate를 자동으로 설정
        if (item.getSeller() == null) {
            throw new IllegalArgumentException("판매자 정보는 필수입니다.");
        }

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

    // 카테고리별 아이템 리스트 가져오기
    public Page<Item> getItemsByCategory(String category, Pageable pageable) {
        return itemRepository.findPageByCategory(category, pageable);
    }

    // 아이템 ID로 가져오기
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
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
        if (updatedItem.getId() == null) {
            throw new IllegalArgumentException("아이템 ID는 null일 수 없습니다.");
        }

        // 아이템이 존재하는지 확인
        Item existingItem = itemRepository.findById(updatedItem.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "아이템을 찾을 수 없습니다."));

        // 기존 아이템의 값들을 업데이트된 값으로 변경
        existingItem.setTitle(updatedItem.getTitle()); // 제목
        existingItem.setPrice(updatedItem.getPrice()); // 가격
        existingItem.setImgURL(updatedItem.getImgURL()); // 이미지 URL
        existingItem.setItemDesc(updatedItem.getItemDesc()); // 설명
        existingItem.setCategory(updatedItem.getCategory()); // 카테고리
        existingItem.setSubcategory(updatedItem.getSubcategory()); // 서브 카테고리
        existingItem.setSituation(updatedItem.getSituation()); // 상태

        // seller와 uploadDate는 변경하지 않도록 설정
        // seller는 변경되지 않도록 그대로 유지
        // uploadDate는 자동으로 설정되므로 변경되지 않도록 자동으로 유지

        return itemRepository.save(existingItem); // 변경된 아이템 저장
    }

    // 아이템 상태 업데이트
    @Transactional
    public void updateItemSituation(Long itemId, Item.ItemSituation situation) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("아이템을 찾을 수 없습니다."));
        item.updateSituation(situation);
        itemRepository.save(item); // 상태 업데이트
    }

    // 아이템 카테고리 수정
    @Transactional
    public void updateItemCategory(Long itemId, String newCategory) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "아이템을 찾을 수 없습니다."));

        // 카테고리만 수정하고, 상태는 그대로 두기
        item.setCategory(newCategory);
        itemRepository.save(item); // 카테고리 수정 사항 저장
    }
}
