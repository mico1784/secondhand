package com.SecondHand.chat.chatMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class DateUtils {

    public static String formatTimestamp(String timestamp) {
        try {
            // 타임스탬프 처리
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, formatter);

            // 오늘 날짜와 비교
            LocalDate today = LocalDate.now();
            if (dateTime.toLocalDate().isEqual(today)) {
                // 오늘일 경우: HH:mm 형식
                return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                // 오늘 이전일 경우: MM.dd 형식
                return dateTime.format(DateTimeFormatter.ofPattern("MM.dd"));
            }
        } catch (Exception e) {
            // 예외 발생 시 기본 형식 처리 (나노초가 없을 경우 등)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, formatter);
            LocalDate today = LocalDate.now();
            if (dateTime.toLocalDate().isEqual(today)) {
                return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                return dateTime.format(DateTimeFormatter.ofPattern("MM.dd"));
            }
        }
    }
}