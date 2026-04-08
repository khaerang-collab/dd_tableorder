package com.tableorder.integration;

import com.tableorder.menu.entity.Category;
import com.tableorder.menu.entity.Menu;
import com.tableorder.store.entity.Store;
import com.tableorder.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SessionLifecycleTest extends AbstractIntegrationTest {

    private Store store;
    private Menu menu;
    private String adminToken;

    @BeforeEach
    void setUp() {
        store = dataFactory.createStore("테스트 매장");
        dataFactory.createAdminUser(store.getId(), "admin", "admin1234");
        Category cat = dataFactory.createCategory(store, "메인", 0);
        menu = dataFactory.createMenu(cat, "불고기", 15000);
        dataFactory.createTable(store, 1);
        adminToken = generateAdminToken(store.getId());
    }

    // E2E-SESS-001: 세션 전체 라이프사이클
    @Test
    @DisplayName("E2E: 세션 생성 → 장바구니 → 주문 → 이용 완료 → 아카이브 → 새 세션")
    void fullSessionLifecycle() throws Exception {
        // 1. 카카오 로그인 → 세션 생성
        MvcResult loginResult = mockMvc.perform(post("/api/customer/auth/kakao-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storeId\": " + store.getId() + ", \"tableNumber\": 1, \"kakaoAccessToken\": \"dev-test-token\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String json = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(json).get("token").asText();
        long sessionId = objectMapper.readTree(json).get("sessionId").asLong();
        long tableId = objectMapper.readTree(json).get("tableId").asLong();

        // 2. 장바구니 추가
        mockMvc.perform(post("/api/customer/sessions/" + sessionId + "/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuId\": " + menu.getId() + ", \"quantity\": 2}"))
                .andExpect(status().isOk());

        // 3. 주문 생성
        mockMvc.perform(post("/api/customer/sessions/" + sessionId + "/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        // 4. 관리자 이용 완료 처리
        mockMvc.perform(post("/api/admin/stores/" + store.getId() + "/tables/" + tableId + "/complete")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. 히스토리 확인
        mockMvc.perform(get("/api/admin/stores/" + store.getId() + "/orders/history")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 6. 새 로그인 → 새 세션 생성
        MvcResult newLogin = mockMvc.perform(post("/api/customer/auth/kakao-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storeId\": " + store.getId() + ", \"tableNumber\": 1, \"kakaoAccessToken\": \"dev-test-token\"}"))
                .andExpect(status().isOk())
                .andReturn();

        long newSessionId = objectMapper.readTree(newLogin.getResponse().getContentAsString())
                .get("sessionId").asLong();
        String newToken = objectMapper.readTree(newLogin.getResponse().getContentAsString())
                .get("token").asText();

        // 새 세션의 주문 내역은 비어있음
        mockMvc.perform(get("/api/customer/sessions/" + newSessionId + "/orders")
                        .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
