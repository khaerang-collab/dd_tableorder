package com.tableorder.auth.controller;

import com.tableorder.auth.entity.AdminUser;
import com.tableorder.store.entity.Store;
import com.tableorder.support.AbstractIntegrationTest;
import com.tableorder.table.entity.RestaurantTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends AbstractIntegrationTest {

    private Store store;

    @BeforeEach
    void setUp() {
        store = dataFactory.createStore("테스트 매장");
        dataFactory.createAdminUser(store.getId(), "admin", "admin1234");
    }

    // IT-AUTH-001: 관리자 로그인 성공
    @Test
    @DisplayName("POST /api/admin/auth/login - 올바른 자격증명으로 로그인 성공")
    void adminLoginSuccess() throws Exception {
        String body = """
            {"storeId": %d, "username": "admin", "password": "admin1234"}
            """.formatted(store.getId());

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.storeId").value(store.getId()))
                .andExpect(jsonPath("$.storeName").value("테스트 매장"))
                .andExpect(jsonPath("$.adminUsername").value("admin"));
    }

    // IT-AUTH-002: 유효성 검증 실패
    @Test
    @DisplayName("POST /api/admin/auth/login - 필수 필드 누락 시 400")
    void adminLoginValidationFail() throws Exception {
        String body = """
            {"storeId": null, "username": "", "password": ""}
            """;

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    // IT-AUTH-003: 비밀번호 길이 미달
    @Test
    @DisplayName("POST /api/admin/auth/login - 비밀번호 8자 미만 시 400")
    void adminLoginPasswordTooShort() throws Exception {
        String body = """
            {"storeId": %d, "username": "admin", "password": "short"}
            """.formatted(store.getId());

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // IT-AUTH-004: 5회 연속 실패 후 계정 잠금
    @Test
    @DisplayName("POST /api/admin/auth/login - 5회 실패 후 6번째에 423 Locked")
    void adminLoginAccountLock() throws Exception {
        String wrongBody = """
            {"storeId": %d, "username": "admin", "password": "wrongpassword"}
            """.formatted(store.getId());

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/admin/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(wrongBody))
                    .andExpect(status().isForbidden());
        }

        // 6번째 시도 - 잠금
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongBody))
                .andExpect(status().is(423));
    }

    // IT-AUTH-005: 카카오 로그인 (dev-test-token)
    @Test
    @DisplayName("POST /api/customer/auth/kakao-login - dev-test-token으로 로그인 성공")
    void kakaoLoginSuccess() throws Exception {
        dataFactory.createTable(store, 1);

        String body = """
            {"storeId": %d, "tableNumber": 1, "kakaoAccessToken": "dev-test-token"}
            """.formatted(store.getId());

        mockMvc.perform(post("/api/customer/auth/kakao-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.sessionId").isNumber())
                .andExpect(jsonPath("$.tableNumber").value(1));
    }

    // IT-AUTH-006: 인증 없이 Admin API 접근
    @Test
    @DisplayName("인증 헤더 없이 Admin API 접근 시 401/403")
    void accessAdminWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/admin/stores/" + store.getId() + "/categories"))
                .andExpect(status().is(oneOf(401, 403)));
    }

    // IT-AUTH-007: Customer 토큰으로 Admin API 접근
    @Test
    @DisplayName("Customer 토큰으로 Admin API 접근 시 403")
    void accessAdminWithCustomerToken() throws Exception {
        String customerToken = generateCustomerToken(store.getId(), 1L, 1L, 1L);

        mockMvc.perform(get("/api/admin/stores/" + store.getId() + "/categories")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @SuppressWarnings("unchecked")
    private static org.hamcrest.Matcher<Integer> oneOf(int... values) {
        org.hamcrest.Matcher<Integer>[] matchers = java.util.Arrays.stream(values)
                .mapToObj(org.hamcrest.Matchers::equalTo)
                .toArray(org.hamcrest.Matcher[]::new);
        return org.hamcrest.Matchers.anyOf(matchers);
    }
}
