package com.tableorder.auth.service;

import com.tableorder.auth.dto.KakaoProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class KakaoOAuthService {

    @Value("${app.kakao.client-id}")
    private String clientId;

    private final RestTemplate restTemplate = new RestTemplate();

    public KakaoProfile getKakaoProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET, entity, Map.class);

        Map<String, Object> body = response.getBody();
        Long kakaoId = ((Number) body.get("id")).longValue();

        Map<String, Object> kakaoAccount = (Map<String, Object>) body.getOrDefault("kakao_account", Map.of());
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.getOrDefault("profile", Map.of());

        return new KakaoProfile(
                kakaoId,
                (String) profile.getOrDefault("nickname", null),
                (String) kakaoAccount.getOrDefault("gender", null),
                (String) kakaoAccount.getOrDefault("age_range", null),
                (String) profile.getOrDefault("profile_image_url", null)
        );
    }
}
