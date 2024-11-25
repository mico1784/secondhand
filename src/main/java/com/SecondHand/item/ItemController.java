package com.SecondHand.item;

import com.SecondHand.review.ReviewService;
import com.SecondHand.review.ReviewDTO;
import com.SecondHand.user.User;
import com.SecondHand.user.UserRepository;
import com.SecondHand.wishList.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ItemController {

    // 필드 정의
    private final ItemRepository itemRepository;
    private final ItemService itemService;
    private final S3Service s3Service; // S3 파일 업로드/삭제 서비스를 담당
    private final UserRepository userRepository;
    private final WishListRepository wishListRepository;

    @Autowired
    private ReviewService reviewService; // 리뷰 관련 서비스

    // 아이템 작성 폼을 보여주는 메서드
    @GetMapping("/item")
    public String showWriteForm(Model model, Principal principal) {
        model.addAttribute("item", new Item()); // 빈 Item 객체를 모델에 추가
        return "item"; // item 작성 페이지 반환
    }

    // 아이템 제출 처리 메서드
    @PostMapping("/item/add")
    public String submitItem(@ModelAttribute Item item,
                             @RequestParam("imgFile") MultipartFile file,
                             Principal principal) throws IOException {
        // 이미지 파일 업로드 여부 확인
        if (file != null && !file.isEmpty()) {
            try {
                String imgURL = s3Service.uploadFile(file); // 파일 업로드 후 URL 반환
                item.setImgURL(imgURL); // 아이템에 이미지 URL 설정
            } catch (RuntimeException e) {
                throw new RuntimeException("파일 업로드 중 오류 발생: " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("파일이 비어있습니다."); // 파일이 없을 경우 예외 발생
        }

        // 현재 로그인한 사용자 정보 설정
        Optional<User> user = userRepository.findByUsername(principal.getName());
        if (user.isPresent()) {
            item.setSeller(user.get()); // 판매자 정보 설정
        } else {
            throw new RuntimeException("판매자 정보를 찾을 수 없습니다."); // 사용자 정보가 없을 경우 예외 발생
        }

        // 거래지역 설정
        String location = item.getLocation();
        if (location != null && !location.isEmpty()) {
            item.setLocation(location);
        } else {
            throw new RuntimeException("거래지역을 입력해주세요."); // 거래지역이 없는 경우 예외 발생
        }

        // 상품 설명 줄바꿈 처리
        String itemDesc = item.getItemDesc();
        if (itemDesc != null && !itemDesc.isEmpty()) {
            item.setItemDesc(itemDesc.replace("\n", "<br>")); // 줄바꿈을 <br> 태그로 변환
        } else {
            throw new RuntimeException("상품 설명을 입력해주세요."); // 상품 설명이 없는 경우 예외 발생
        }

        // 아이템 저장
        itemService.saveItem(item);
        return "redirect:/list"; // 아이템 목록 페이지로 리다이렉트
    }


    // 아이템 삭제 메서드 (판매자만 가능)
    @PostMapping("/item/delete")
    public String deleteItem(@RequestParam Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (!itemService.hasEditPermission(id, principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
            return "redirect:/list"; // 권한이 없는 경우 홈으로 리다이렉트
        }

        try {
            Item item = itemService.getItemById(id);
            String fileName = item.getImgURL().substring(item.getImgURL().lastIndexOf("/") + 1); // 파일 이름 추출
            s3Service.deleteFile(fileName); // S3에서 파일 삭제
            itemService.deleteItem(id); // 데이터베이스에서 아이템 삭제
            redirectAttributes.addFlashAttribute("message", "아이템이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "아이템 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/home"; // 홈으로 리다이렉트
    }

    @GetMapping("/item/{id}")
    public String showItemDetail(@PathVariable Long id, Model model, Principal principal, @RequestParam(defaultValue = "1") Integer page) {
        Item item = itemService.getItemById(id);

        if (item != null) {
            // 로그인 여부 및 사용자 정보 확인
            boolean isLoggedIn = (principal != null);
            String currentUsername = isLoggedIn ? principal.getName() : "anonymous";
            User currentUser = isLoggedIn ? userRepository.findByUsername(currentUsername).orElse(null) : null;

            // 아이템 등록일 포맷팅
            if (item.getUploadDate() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String formattedDate = item.getUploadDate().format(formatter);
                model.addAttribute("formattedDate", formattedDate);
            } else {
                model.addAttribute("formattedDate", "날짜 정보 없음");
            }

            // 상품 설명 줄바꿈 처리
            if (item.getItemDesc() != null) {
                String formattedDesc = item.getItemDesc().replace("\n", "<br>");
                model.addAttribute("formattedItemDesc", formattedDesc);
            } else {
                model.addAttribute("formattedItemDesc", "상품 설명 없음");
            }

            // 판매자 정보 추가
            User seller = item.getSeller();
            model.addAttribute("seller", seller);
            model.addAttribute("sellerProfileImageURL", seller.getProfileImageURL());

            // 찜 상태 확인 (로그인된 경우만 처리)
            boolean isWished = false;
            if (isLoggedIn && currentUser != null) {
                isWished = wishListRepository.findByItemIdAndUser(item.getId(), currentUser).isPresent();
            }
            model.addAttribute("isLoggedIn", isLoggedIn);
            model.addAttribute("isWished", isWished);

            // 리뷰 작성자의 정보 포함한 리뷰 리스트 가져오기
            List<ReviewDTO> reviews = reviewService.getReviewsBySellerId(seller.getId());
            reviews.forEach(review -> {
                review.setProfileImageURL(review.getProfileImageURL());
            });

            // 모델에 필요한 데이터 추가
            model.addAttribute("item", item);
            model.addAttribute("currentUsername", currentUsername);
            model.addAttribute("user", currentUser);
            model.addAttribute("reviews", reviews);

            // 카테고리별 아이템 리스트 추가 (최신순으로 정렬)
            List<Item> phoneItems = itemService.getAllItemsByCategorySorted("휴대폰", Sort.by(Sort.Direction.DESC, "uploadDate"));
            model.addAttribute("phoneItems", phoneItems);

            List<Item> padItems = itemService.getAllItemsByCategorySorted("패드", Sort.by(Sort.Direction.DESC, "uploadDate"));
            model.addAttribute("padItems", padItems);

            List<Item> watchItems = itemService.getAllItemsByCategorySorted("워치", Sort.by(Sort.Direction.DESC, "uploadDate"));
            model.addAttribute("watchItems", watchItems);

            return "itemDetail"; // 상세 페이지로 이동
        } else {
            model.addAttribute("error", "아이템을 찾을 수 없습니다.");
            return "itemList"; // 아이템이 없으면 목록 페이지로 돌아감
        }
    }


    // 아이템 수정 폼 표시 메서드
    @GetMapping("/item/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal) {
        if (!itemService.hasEditPermission(id, principal.getName())) {
            return "redirect:/home"; // 권한이 없으면 홈으로 리다이렉트
        }
        Item item = itemService.getItemById(id);
        model.addAttribute("item", item); // 수정할 아이템 정보 모델에 추가
        return "itemEdit"; // 수정 페이지 반환
    }

    // 아이템 수정 처리 메서드
    @PostMapping("/item/edit/{id}")
    public String updateItem(@PathVariable Long id,
                             @ModelAttribute Item item,
                             @RequestParam(value = "imgFile", required = false) MultipartFile file,
                             Principal principal,
                             RedirectAttributes redirectAttributes) throws IOException {
        if (!itemService.hasEditPermission(id, principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/home"; // 권한이 없으면 홈으로 리다이렉트
        }

        Item existingItem = itemService.getItemById(id);
        if (item.getLocation() != null) existingItem.setLocation(item.getLocation());
        if (item.getCategory() != null) existingItem.setCategory(item.getCategory());
        if (item.getSubcategory() != null) existingItem.setSubcategory(item.getSubcategory());

        // 이미지 파일 처리
        if (file != null && !file.isEmpty()) {
            try {
                if (existingItem.getImgURL() != null) {
                    String fileName = existingItem.getImgURL().substring(existingItem.getImgURL().lastIndexOf("/") + 1);
                    s3Service.deleteFile("SecondHand/" + fileName);
                }
                String imgURL = s3Service.uploadFile(file);
                existingItem.setImgURL(imgURL);
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류 발생: " + e.getMessage());
                return "redirect:/item/edit/" + id; // 오류 발생 시 수정 페이지로 돌아감
            }
        }

        if (item.getTitle() != null) existingItem.setTitle(item.getTitle());
        if (item.getPrice() != null) existingItem.setPrice(item.getPrice());
        if (item.getItemDesc() != null) existingItem.setItemDesc(item.getItemDesc());
        if (item.getSituation() != null) existingItem.setSituation(item.getSituation());

        itemService.updateItem(existingItem);
        redirectAttributes.addFlashAttribute("message", "아이템이 성공적으로 수정되었습니다.");
        return "redirect:/item/" + existingItem.getId(); // 상세 페이지로 리다이렉트
    }

    // 아이템 목록 페이지 표시
    @GetMapping("/list")
    public String categoryList(Model m,
                               @RequestParam(required = false, defaultValue = "") String category,
                               @RequestParam(required = false) Integer minPrice,
                               @RequestParam(required = false) Integer maxPrice,
                               @RequestParam(defaultValue = "1") Integer page) {
        Page<Item> list;

        // 카테고리 필터링
        if (category.isEmpty()) {
            list = itemRepository.findAll(PageRequest.of(page - 1, 24, Sort.by(Sort.Direction.DESC, "id")));
        } else {
            list = itemRepository.findPageByCategory(category, PageRequest.of(page - 1, 24, Sort.by(Sort.Direction.DESC, "id")));
            m.addAttribute("category", category);
        }

        // 가격 필터링 적용
        if (minPrice != null || maxPrice != null) {
            list = new PageImpl<>(list.stream()
                    .filter(item -> (minPrice == null || item.getPrice() >= minPrice) &&
                            (maxPrice == null || item.getPrice() <= maxPrice))
                    .collect(Collectors.toList()));
        }

        // 아이템 목록에 포맷된 가격 추가
        List<Item> items = list.getContent();
        items.forEach(item -> item.setFormattedPrice(String.format("%,d", item.getPrice())));

        // 가격 통계 계산 후 모델에 추가
        if (!items.isEmpty()) {
            int maxPriceValue = items.stream().mapToInt(Item::getPrice).max().orElse(0);
            int minPriceValue = items.stream().mapToInt(Item::getPrice).min().orElse(0);
            double avgPriceValue = items.stream().mapToInt(Item::getPrice).average().orElse(0.0);

            // 포맷팅된 가격 통계
            String formattedMaxPrice = String.format("%,d", maxPriceValue);
            String formattedMinPrice = String.format("%,d", minPriceValue);
            String formattedAvgPrice = String.format("%,d", (int) avgPriceValue);

            // 모델에 값 추가
            m.addAttribute("formattedMaxPrice", formattedMaxPrice);
            m.addAttribute("formattedMinPrice", formattedMinPrice);
            m.addAttribute("formattedAvgPrice", formattedAvgPrice);

            // 실제 가격 값도 모델에 추가 (원하는 경우)
            m.addAttribute("maxPriceValue", maxPriceValue);
            m.addAttribute("minPriceValue", minPriceValue);
            m.addAttribute("avgPriceValue", (int) avgPriceValue);
        } else {
            m.addAttribute("formattedMaxPrice", "0");
            m.addAttribute("formattedMinPrice", "0");
            m.addAttribute("formattedAvgPrice", "0");

            // 실제 가격 값도 모델에 추가 (원하는 경우)
            m.addAttribute("maxPriceValue", 0);
            m.addAttribute("minPriceValue", 0);
            m.addAttribute("avgPriceValue", 0);
        }

        // 페이지 관련 정보
        m.addAttribute("items", items);
        m.addAttribute("hasPrevious", list.hasPrevious());
        m.addAttribute("hasNext", list.hasNext());
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPage", list.getTotalPages());

        return "list1.html"; // 목록 페이지 반환
    }

    // 검색 결과 처리 메서드
    private String processSearchList(Model m, String searchText, Integer page) {
        Page<Item> list = itemRepository.findPageByTitleContains(searchText, PageRequest.of(page - 1, 24, Sort.by(Sort.Direction.DESC, "id")));
        if (list.isEmpty()) {
            m.addAttribute("warningMessage", "검색 결과가 없습니다."); // 검색 결과 없음 경고 메시지
        }

        m.addAttribute("searchText", searchText);
        m.addAttribute("items", list.getContent());
        m.addAttribute("hasPrevious", list.hasPrevious());
        m.addAttribute("hasNext", list.hasNext());
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPage", list.getTotalPages());

        return "search.html"; // 검색 결과 페이지 반환
    }

    @PostMapping("/list/{searchText}")
    public String searchListPost(Model m, @PathVariable String searchText, @RequestParam(defaultValue = "1") Integer page) {
        return processSearchList(m, searchText, page);
    }

    @GetMapping("/list/{searchText}")
    public String searchListGet(Model m, @PathVariable String searchText, @RequestParam(defaultValue = "1") Integer page) {
        return processSearchList(m, searchText, page);
    }

    // 아이템 상태 업데이트 메서드
    @PutMapping("/item/{id}/situation")
    public ResponseEntity<String> updateItemSituation(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String situationString = request.get("situation");
        if (situationString == null) {
            return ResponseEntity.badRequest().body("Situation value cannot be null");
        }

        try {
            Item.ItemSituation situation = Item.ItemSituation.valueOf(situationString); // Enum 값 확인
            itemService.updateItemSituation(id, situation);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid situation value");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating situation");
        }
    }

    // 아이템 상세 페이지 (판매자 리뷰 포함)
    @GetMapping("/item/detail/{itemId}")
    public String getItemDetail(@PathVariable Long itemId, Model model) {
        Item item = itemService.getItemById(itemId);
        User seller = item.getSeller(); // 판매자 정보 가져오기

        List<ReviewDTO> reviews = reviewService.getReviewsBySellerId(seller.getId()); // 리뷰 가져오기

        model.addAttribute("item", item);
        model.addAttribute("reviews", reviews);

        return "item/detail"; // 상세 페이지 반환
    }

    @GetMapping("/item/{id}/exists")
    public ResponseEntity<Void> checkItemExists(@PathVariable Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isPresent()) {
            return ResponseEntity.ok().build(); // 존재하면 200 응답
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 존재하지 않으면 404 응답
        }
    }
}
