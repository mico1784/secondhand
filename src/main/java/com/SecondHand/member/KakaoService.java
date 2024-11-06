package com.SecondHand.member;

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
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    // 액세스 토큰 발급 요청 메서드
    public String getKakaoAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId); // 설정 파일에서 불러온 clientId 사용
        params.add("redirect_uri", redirectUri); // 설정 파일에서 불러온 redirectUri 사용
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    // 사용자 정보 요청 메서드는 그대로 유지
    public Map<String, Object> getKakaoUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        Map<String, Object> userInfo = new HashMap<>();

        if (responseBody != null) {
            userInfo.put("id", responseBody.get("id"));

            Map<String, Object> properties = (Map<String, Object>) responseBody.get("properties");
            if (properties != null && properties.get("nickname") != null) {
                userInfo.put("nickname", properties.get("nickname"));
            }
        }
        return userInfo;
    }
}
