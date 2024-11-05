package com.SecondHand.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Page<Item> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    public Page<Item> getItemsByCategory(String category, Pageable pageable) {
        return itemRepository.findPageByCategory(category, pageable);
    }

    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "아이템을 찾을 수 없습니다."));
    }

    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    public Item updateItem(Item item) {
        if (item.getId() == null) {
            throw new IllegalArgumentException("아이템 ID는 null일 수 없습니다.");
        }

        if (itemRepository.existsById(item.getId())) {
            return itemRepository.save(item);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "아이템을 찾을 수 없습니다.");
        }
    }
}