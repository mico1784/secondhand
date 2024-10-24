package com.SecondHand.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll(); // 모든 아이템 목록을 반환
    }

    // 전체 아이템을 페이지네이션으로 가져오는 메소드 추가
    public Page<Item> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable); // 페이지네이션된 아이템 목록을 반환
    }

    // 카테고리별 아이템을 페이지네이션으로 가져오는 메소드 추가
    public Page<Item> getItemsByCategory(String category, Pageable pageable) {
        return itemRepository.findPageByCategory(category, pageable); // 카테고리별로 페이지네이션된 아이템 목록 반환
    }

    public Item getItemById(Long id) {
        Optional<Item> item = itemRepository.findById(id); // 아이템 조회
        return item.orElse(null); // 아이템이 없으면 null 반환
    }

    public void deleteItem(Long id) {
        itemRepository.deleteById(id); // 아이템을 데이터베이스에서 삭제
    }
}
