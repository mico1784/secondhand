package com.SecondHand.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoService {

    @Value("${kakao.client-id}")
    private String clientId; // 카카오 클라이언트 ID (설정 파일에서 불러옴)

    @Value("${kakao.redirect-uri}")
    private String redirectUri; // 카카오 리다이렉트 URI (설정 파일에서 불러옴)

    // 카카오 액세스 토큰 발급 요청 메서드
    public String getKakaoAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate(); // HTTP 요청을 보내기 위한 RestTemplate 객체 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // 요청의 콘텐츠 타입을 설정

        // 요청 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code"); // OAuth 2.0 인증 코드 타입 설정
        params.add("client_id", clientId); // 클라이언트 ID
        params.add("redirect_uri", redirectUri); // 리다이렉트 URI
        params.add("code", code); // 인증 코드

        // 요청 엔티티 생성
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 카카오 토큰 요청 URL에 POST 요청 보내기
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", request, Map.class);

        // 응답에서 액세스 토큰 추출
        return (String) response.getBody().get("access_token");
    }

    // 카카오 사용자 정보 요청 메서드
    public Map<String, Object> getKakaoUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate(); // HTTP 요청을 보내기 위한 RestTemplate 객체 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken); // Bearer 토큰 방식으로 인증 헤더 추가

        // 요청 엔티티 생성
        HttpEntity<String> request = new HttpEntity<>(headers);

        // 사용자 정보 요청 URL에 GET 요청 보내기
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, request, Map.class);

        // 응답 본문에서 사용자 정보 추출
        Map<String, Object> responseBody = response.getBody();
        Map<String, Object> userInfo = new HashMap<>();

        if (responseBody != null) {
            userInfo.put("id", responseBody.get("id")); // 사용자 ID 저장

            // 프로퍼티에서 닉네임 추출
            Map<String, Object> properties = (Map<String, Object>) responseBody.get("properties");
            if (properties != null && properties.get("nickname") != null) {
                userInfo.put("nickname", properties.get("nickname")); // 사용자 닉네임 저장
            }
        }
        return userInfo; // 사용자 정보 반환
    }
}
