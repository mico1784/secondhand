package com.SecondHand.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CookieUtil {
    private static final String ALGORITHM = "AES"; // 암호화 알고리즘으로 AES 사용
    private static final String SECRET_KEY = "YourSecretKey123"; // 16바이트 비밀키 (AES는 16, 24, 32바이트 키를 지원)

    // 문자열을 암호화하는 메서드
    public static String encrypt(String value) throws Exception {
        // 비밀키 생성 (AES 알고리즘을 위한 SecretKeySpec)
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        // 암호화 알고리즘(Cipher) 초기화
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key); // 암호화 모드 설정
        // 입력된 문자열을 암호화하고 Base64로 인코딩하여 반환
        byte[] encrypted = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // 암호화된 문자열을 복호화하는 메서드
    public static String decrypt(String value) throws Exception {
        // 비밀키 생성 (AES 알고리즘을 위한 SecretKeySpec)
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        // 암호화 알고리즘(Cipher) 초기화
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key); // 복호화 모드 설정
        // Base64로 인코딩된 암호화 문자열을 디코딩하고 복호화하여 원본 문자열 반환
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(value));
        return new String(decrypted);
    }
}
