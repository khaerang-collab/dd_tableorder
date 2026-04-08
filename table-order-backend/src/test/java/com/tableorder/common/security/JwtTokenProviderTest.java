package com.tableorder.common.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
                57600000L // 16시간
        );
    }

    // UT-JWT-001: 토큰 생성 및 검증
    @Test
    @DisplayName("유효한 JWT 토큰을 생성하고 검증할 수 있다")
    void createAndValidateToken() {
        Map<String, Object> claims = Map.of(
                "sub", "1",
                "storeId", 1,
                "role", "ADMIN"
        );

        String token = jwtTokenProvider.createToken(claims);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();

        Claims parsed = jwtTokenProvider.getClaims(token);
        assertThat(parsed.getSubject()).isEqualTo("1");
        assertThat(parsed.get("storeId", Integer.class)).isEqualTo(1);
        assertThat(parsed.get("role", String.class)).isEqualTo("ADMIN");
    }

    // UT-JWT-002: 만료된 토큰 검증
    @Test
    @DisplayName("만료된 JWT 토큰은 검증에 실패한다")
    void expiredTokenValidation() {
        JwtTokenProvider shortLived = new JwtTokenProvider(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
                0L // 즉시 만료
        );

        String token = shortLived.createToken(Map.of("sub", "1", "role", "ADMIN"));

        assertThat(shortLived.validateToken(token)).isFalse();
    }

    // UT-JWT-003: 변조된 토큰 검증
    @Test
    @DisplayName("변조된 JWT 토큰은 검증에 실패한다")
    void tamperedTokenValidation() {
        String token = jwtTokenProvider.createToken(Map.of("sub", "1", "role", "ADMIN"));

        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }
}
