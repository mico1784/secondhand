package com.SecondHand.chat.Base64;

import com.SecondHand.item.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileService {

    private final S3Service s3Service;

    public FileService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    // 파일 업로드 메서드
    public String uploadFile(MultipartFile file) throws IOException {
        // 파일을 S3에 업로드하고, S3 URL을 반환
        return s3Service.uploadFile(file);
    }
}
