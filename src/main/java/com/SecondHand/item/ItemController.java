package com.SecondHand.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final ItemService itemService;
    private final S3Service s3Service;

    // 아이템 작성 폼 보여주기
    @GetMapping("/item")
    public String showWriteForm(Model model) {
        model.addAttribute("item", new Item());
        return "item";
    }

    // 아이템 제출
    @PostMapping("/item/add")
    public String submitItem(@ModelAttribute Item item, @RequestParam("imgFile") MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            try {
                String imgURL = s3Service.uploadFile(file);
                item.setImgURL(imgURL);
            } catch (RuntimeException e) {
                throw new RuntimeException("파일 업로드 중 오류 발생: " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("파일이 비어있습니다.");
        }

        itemService.saveItem(item);
        return "redirect:/list";
    }

    // 아이템 삭제
    @PostMapping("/item/delete")
    public String deleteItem(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Item item = itemService.getItemById(id); // 아이템 조회
        if (item != null) {
            try {
                // S3에서 파일 삭제
                String fileName = item.getImgURL().substring(item.getImgURL().lastIndexOf("/") + 1); // 파일 이름 추출
                s3Service.deleteFile(fileName); // S3에서 파일 삭제
                itemService.deleteItem(id); // 데이터베이스에서 아이템 삭제

                // 성공 메시지 추가
                redirectAttributes.addFlashAttribute("message", "아이템이 성공적으로 삭제되었습니다.");
            } catch (Exception e) {
                // 오류 메시지 추가
                redirectAttributes.addFlashAttribute("error", "아이템 삭제 중 오류가 발생했습니다.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "아이템을 찾을 수 없습니다.");
        }
        return "redirect:/home"; // 목록 페이지로 리다이렉트
    }


    // 상품 상세 정보 페이지로 이동
    @GetMapping("/item/{id}")
    public String showItemDetail(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        if (item != null) {
            // uploadDate가 null이 아닐 경우에만 포맷팅
            if (item.getUploadDate() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String formattedDate = item.getUploadDate().format(formatter);
                model.addAttribute("formattedDate", formattedDate); // 포맷된 날짜 전달
            } else {
                model.addAttribute("formattedDate", "날짜 정보 없음"); // 대체 문자열 추가
            }
            model.addAttribute("item", item);
            return "itemDetail"; // 디테일 페이지로 이동
        } else {
            model.addAttribute("error", "아이템을 찾을 수 없습니다.");
            return "itemList"; // 목록 페이지로 돌아감
        }
    }


    // S3 presigned URL 생성
    @GetMapping("/presigned-url")
    @ResponseBody
    public String getPresignedUrl(@RequestParam String filename) {
        var result = s3Service.createPreSignedUrl("SecondHand/" + filename);
        System.out.println(result);
        return result;
    }

    // 아이템 수정 폼 보여주기
    @GetMapping("/item/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        if (item != null) {
            model.addAttribute("item", item);
            return "itemEdit"; // 수정 페이지로 이동
        } else {
            model.addAttribute("error", "아이템을 찾을 수 없습니다.");
            return "itemList"; // 목록 페이지로 돌아감
        }
    }


    // 아이템 수정 처리
    @PostMapping("/item/edit/{id}")
    public String updateItem(@PathVariable Long id,
                             @ModelAttribute Item item,
                             @RequestParam(value = "imgFile", required = false) MultipartFile file,
                             RedirectAttributes redirectAttributes) throws IOException {
        // 기존 아이템 조회
        Item existingItem = itemService.getItemById(id); // id를 사용하여 조회

        // 파일이 업로드된 경우
        if (file != null && !file.isEmpty()) {
            try {
                // S3에서 기존 이미지 삭제
                if (existingItem.getImgURL() != null) {
                    String fileName = existingItem.getImgURL().substring(existingItem.getImgURL().lastIndexOf("/") + 1);
                    s3Service.deleteFile("SecondHand/" + fileName); // 기존 이미지 삭제
                }
                // 새 이미지 업로드
                String imgURL = s3Service.uploadFile(file);
                item.setImgURL(imgURL); // 새 이미지 URL 설정
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류 발생: " + e.getMessage());
                return "redirect:/item/edit/" + id; // 오류 발생 시 수정 페이지로 돌아감
            }
        } else {
            // 파일이 업로드되지 않은 경우, 기존 이미지 URL 유지
            item.setImgURL(existingItem.getImgURL());
        }

        // 아이템 업데이트
        item.setId(existingItem.getId()); // 아이템의 ID를 설정 (필수)
        itemService.updateItem(item);

        // 수정 완료 후 상세 페이지로 리다이렉트
        redirectAttributes.addFlashAttribute("message", "아이템이 성공적으로 수정되었습니다.");
        return "redirect:/item/" + existingItem.getId(); // 상세 페이지로 리다이렉트
    }


    // 목록
    @GetMapping("/list")
    String categoryList(Model m,
                        @RequestParam(required = false, defaultValue = "") String category,
                        @RequestParam(defaultValue = "1") Integer page){
        Page<Item> list;

        if(category.isEmpty()){ // 정해진 카테고리가 없으면 전체 목록을 반환
            list = itemRepository.findAll(PageRequest.of(page -1, 3, Sort.by(Sort.Direction.DESC, "id")));
        }else{  // 정해진 카테고리가 있다면
            list = itemRepository.findPageByCategory(category, PageRequest.of(page -1, 3, Sort.by(Sort.Direction.DESC, "id")));
            m.addAttribute("category", category);
        }
        m.addAttribute("items", list.getContent());
        m.addAttribute("hasPrevious", list.hasPrevious());
        m.addAttribute("hasNext", list.hasNext());
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPage", list.getTotalPages());

        return "itemList.html";
    }

    // 검색
    private String processSearchList(Model m, String searchText, Integer page) {
        System.out.println(searchText);

        Page<Item> list = itemRepository.findPageByTitleContains(searchText, PageRequest.of(page - 1, 3, Sort.by(Sort.Direction.DESC, "id")));

        if (list.isEmpty()) {
            m.addAttribute("warningMessage", "검색 결과가 없습니다.");
        }

        m.addAttribute("searchText", searchText);
        m.addAttribute("items", list.getContent());
        m.addAttribute("hasPrevious", list.hasPrevious());
        m.addAttribute("hasNext", list.hasNext());
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPage", list.getTotalPages());

        return "search.html";
    }

    @PostMapping("/list/{searchText}")
    public String searchListPost(Model m, @PathVariable String searchText, @RequestParam(defaultValue = "1") Integer page) {
        return processSearchList(m, searchText, page);
    }

    @GetMapping("/list/{searchText}")
    public String searchListGet(Model m, @PathVariable String searchText, @RequestParam(defaultValue = "1") Integer page) {
        return processSearchList(m, searchText, page);
    }

    private String test(){
        return null;
    }
}
