package com.SecondHand.chat.controller;

import com.SecondHand.chat.handler.SocketHandler;
import com.SecondHand.chat.room.Room;
import com.SecondHand.item.Item;
import com.SecondHand.item.ItemRepository;
import com.SecondHand.item.S3Service;  // S3Service 임포트
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@Controller
@RequestMapping("/chat")
public class MainController {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private SocketHandler socketHandler;
    @Autowired
    private S3Service s3Service;  // S3Service로 수정

    // 채팅 페이지 표시
    @RequestMapping("/{itemId}")
    public String chatView(@PathVariable Long itemId, Model m, Authentication auth, Principal principal) {
        if (principal != null) {
            String username = auth.getName();  // 현재 로그인한 사용자 이름
            m.addAttribute("username", username);

            // 아이템 정보 가져오기
            Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Invalid item ID"));

            // 사용자와 상품을 기반으로 고유한 채팅방 번호 생성
            String roomNo = itemId + "-" + username;  // itemId와 username을 결합한 고유한 roomNo

            // 채팅방 생성 또는 기존 채팅방 가져오기
//            Room room = socketHandler.createOrGetRoom(item, roomNo);  // roomNo를 String으로 사용
            m.addAttribute("roomNo", roomNo);
            m.addAttribute("item", item);

            return "chat";
        } else {
            return "redirect:/login";  // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
        }
    }

    // 이미지 업로드 처리
    @PostMapping("/uploadImage")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                         @RequestParam("roomNo") String roomNo,
                                         @RequestParam("username") String username,
                                         @RequestParam("sessionId") String sessionId,
                                         @RequestParam("msg") String msg) {
        // S3Service를 통해 파일 업로드 후 URL 반환
        String fileUrl = s3Service.uploadFile(file);  // 파일을 S3에 업로드
        return ResponseEntity.ok(new UploadResponse(true, fileUrl));
    }

    // 파일 업로드 응답 객체
    public static class UploadResponse {
        private boolean success;
        private String fileUrl;

        public UploadResponse(boolean success, String fileUrl) {
            this.success = success;
            this.fileUrl = fileUrl;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }
    }
}
