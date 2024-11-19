package com.SecondHand.chat.controller;

import com.SecondHand.chat.handler.SocketHandler;
import com.SecondHand.chat.room.Room;
import com.SecondHand.chat.room.RoomRepository;
import com.SecondHand.chat.room.RoomService;
import com.SecondHand.item.ItemRepository;
import com.SecondHand.item.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class MainController {

    private final S3Service s3Service;
    private final ItemRepository itemRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;

    @RequestMapping("/{itemId}")
    public String chatView(@PathVariable Long itemId, Model m, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            m.addAttribute("username", username);

            String roomNo = getRoomNoByItemId(itemId);
            if (roomNo == null) {
                // getRoomOrCreate로 방을 먼저 생성하면서 sellerId와 buyerId를 설정합니다.
                roomNo = generateRandomString(10);
                Room room = roomService.getRoomOrCreate(roomNo, itemId, username);  // sellerId와 buyerId가 올바르게 설정됩니다.
                saveRoomMapping(itemId, roomNo);
            }

            m.addAttribute("roomNo", roomNo);
            m.addAttribute("itemId", itemId);

            return "chat";
        } else {
            return "redirect:/login";
        }
    }

    private String getRoomNoByItemId(Long itemId) {
        Room room = roomRepository.findByItemC_Id(itemId);
        return room != null ? room.getRoomNo() : null;
    }

    private void saveRoomMapping(Long itemId, String roomNo) {
        if (roomRepository.findByRoomNo(roomNo) != null) {
            return;
        }
        Room room = new Room();
        room.setRoomNo(roomNo);
        room.setItemC(itemRepository.findById(itemId).orElseThrow(() -> new NoSuchElementException("Item not found")));
        roomRepository.save(room);
    }

    // 이미지 업로드 처리
    @PostMapping("/uploadImage")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                         @RequestParam("roomNo") String roomNo,
                                         @RequestParam("username") String username,
                                         @RequestParam("sessionId") String sessionId,
                                         @RequestParam("msg") String msg) {
        String fileUrl = s3Service.uploadFile(file);
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

    //랜덤 문자 생성기
    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
