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
        return "redirect:/list";
    }

    // 상품 상세 정보 페이지로 이동
    @GetMapping("/item/{id}")
    public String showItemDetail(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        if (item != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDate = item.getUploadDate().format(formatter);
            model.addAttribute("item", item);
            model.addAttribute("formattedDate", formattedDate); // 포맷된 날짜 전달
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
