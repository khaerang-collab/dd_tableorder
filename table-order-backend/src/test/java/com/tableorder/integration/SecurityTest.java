package com.tableorder.integration;

import com.tableorder.menu.entity.Category;
import com.tableorder.menu.entity.Menu;
import com.tableorder.store.entity.Store;
import com.tableorder.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SecurityTest extends AbstractIntegrationTest {

    private Store store1;
    private Store store2;
    private String admin1Token;
    private String admin2Token;
    private Category store2Category;

    @BeforeEach
    void setUp() {
        store1 = dataFactory.createStore("매장1");
        store2 = dataFactory.createStore("매장2");
        dataFactory.createAdminUser(store1.getId(), "admin1", "admin1234");
        dataFactory.createAdminUser(store2.getId(), "admin2", "admin1234");
        admin1Token = generateAdminToken(store1.getId());
        admin2Token = generateAdminToken(store2.getId());
        store2Category = dataFactory.createCategory(store2, "매장2 카테고리", 0);
    }

    // SEC-001: 만료된 JWT로 API 호출
    @Test
    @DisplayName("만료된 JWT 토큰으로 Admin API 호출 시 인증 실패")
    void expiredJwtToken() throws Exception {
        // 즉시 만료되는 토큰 생성을 위해 유효하지 않은 토큰 사용
        String fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZXhwIjoxfQ.invalid";

        mockMvc.perform(get("/api/admin/stores/" + store1.getId() + "/categories")
                        .header("Authorization", "Bearer " + fakeToken))
                .andExpect(status().is4xxClientError());
    }

    // SEC-002: 변조된 JWT로 API 호출
    @Test
    @DisplayName("변조된 JWT 토큰으로 API 호출 시 인증 실패")
    void tamperedJwtToken() throws Exception {
        String validToken = admin1Token;
        String tampered = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        mockMvc.perform(get("/api/admin/stores/" + store1.getId() + "/categories")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().is4xxClientError());
    }

    // SEC-003: IDOR - 다른 매장의 카테고리 접근
    @Test
    @DisplayName("IDOR: Store1 Admin이 Store2의 카테고리를 수정할 수 없다")
    void idorCategoryAccess() throws Exception {
        mockMvc.perform(put("/api/admin/stores/" + store2.getId() + "/categories/" + store2Category.getId())
                        .header("Authorization", "Bearer " + admin1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"악의적 수정\"}"))
                .andExpect(status().is4xxClientError());
    }

    // SEC-004: IDOR - 다른 매장의 메뉴 삭제
    @Test
    @DisplayName("IDOR: Store1 Admin이 Store2의 메뉴를 삭제할 수 없다")
    void idorMenuDelete() throws Exception {
        Menu store2Menu = dataFactory.createMenu(store2Category, "매장2 메뉴", 10000);

        mockMvc.perform(delete("/api/admin/menus/" + store2Menu.getId())
                        .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().is4xxClientError());
    }

    // SEC-007: SQL Injection 시도 - 로그인
    @Test
    @DisplayName("SQL Injection 시도 시 정상적인 인증 실패로 처리된다")
    void sqlInjectionLogin() throws Exception {
        String body = """
            {"storeId": %d, "username": "admin' OR '1'='1", "password": "' OR '1'='1--"}
            """.formatted(store1.getId());

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    // SEC-009: JWT 없이 public API 접근 확인
    @Test
    @DisplayName("인증 없이 공개 API(메뉴 조회)에 접근할 수 있다")
    void publicApiAccessWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/stores/" + store1.getId() + "/menus"))
                .andExpect(status().isOk());
    }

    // SEC-010: Admin 토큰으로 Customer API 접근
    @Test
    @DisplayName("Admin 토큰으로 Customer API에 접근할 수 없다")
    void adminTokenOnCustomerApi() throws Exception {
        mockMvc.perform(get("/api/customer/sessions/1/cart")
                        .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isForbidden());
    }
}
