package com.tableorder.integration;

import com.tableorder.menu.entity.Category;
import com.tableorder.menu.entity.Menu;
import com.tableorder.store.entity.Store;
import com.tableorder.support.AbstractIntegrationTest;
import com.tableorder.table.entity.RestaurantTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CustomerOrderFlowTest extends AbstractIntegrationTest {

    private Store store;
    private Menu menu1;
    private Menu menu2;

    @BeforeEach
    void setUp() {
        store = dataFactory.createStore("테스트 매장");
        Category category = dataFactory.createCategory(store, "메인", 0);
        menu1 = dataFactory.createMenu(category, "불고기", 15000);
        menu2 = dataFactory.createMenu(category, "비빔밥", 10000);
        dataFactory.createTable(store, 1);
    }

    // E2E-CUST-001: 고객 전체 주문 플로우
    @Test
    @DisplayName("E2E: 카카오 로그인 → 메뉴 조회 → 장바구니 추가 → 주문 생성 → 주문 조회")
    void fullCustomerOrderFlow() throws Exception {
        // 1. 카카오 로그인
        String loginBody = """
            {"storeId": %d, "tableNumber": 1, "kakaoAccessToken": "dev-test-token"}
            """.formatted(store.getId());

        MvcResult loginResult = mockMvc.perform(post("/api/customer/auth/kakao-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        String json = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(json).get("token").asText();
        long sessionId = objectMapper.readTree(json).get("sessionId").asLong();

        // 2. 메뉴 조회
        mockMvc.perform(get("/api/stores/" + store.getId() + "/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].menus", hasSize(2)));

        // 3. 장바구니 추가
        mockMvc.perform(post("/api/customer/sessions/" + sessionId + "/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuId\": " + menu1.getId() + ", \"quantity\": 2}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/customer/sessions/" + sessionId + "/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuId\": " + menu2.getId() + ", \"quantity\": 1}"))
                .andExpect(status().isOk());

        // 4. 장바구니 확인
        mockMvc.perform(get("/api/customer/sessions/" + sessionId + "/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.totalAmount").value(40000)); // 15000*2 + 10000*1

        // 5. 주문 생성
        mockMvc.perform(post("/api/customer/sessions/" + sessionId + "/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(40000));

        // 6. 주문 조회
        mockMvc.perform(get("/api/customer/sessions/" + sessionId + "/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 7. 장바구니 비어있음 확인
        mockMvc.perform(get("/api/customer/sessions/" + sessionId + "/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    // E2E-CUST-002: 장바구니 UPSERT 동작 검증
    @Test
    @DisplayName("E2E: 동일 기기에서 같은 메뉴 추가 시 수량 누적")
    void cartUpsertFlow() throws Exception {
        String loginBody = """
            {"storeId": %d, "tableNumber": 1, "kakaoAccessToken": "dev-test-token"}
            """.formatted(store.getId());

        MvcResult loginResult = mockMvc.perform(post("/api/customer/auth/kakao-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        String json = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(json).get("token").asText();
        long sessionId = objectMapper.readTree(json).get("sessionId").asLong();

        // 1차 추가: 수량 2
        mockMvc.perform(post("/api/customer/sessions/" + sessionId + "/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuId\": " + menu1.getId() + ", \"quantity\": 2}"))
                .andExpect(status().isOk());

        // 2차 추가: 수량 3 (UPSERT → 총 5)
        mockMvc.perform(post("/api/customer/sessions/" + sessionId + "/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuId\": " + menu1.getId() + ", \"quantity\": 3}"))
                .andExpect(status().isOk());

        // 검증: 아이템 1개, 수량 5, 총액 75000
        mockMvc.perform(get("/api/customer/sessions/" + sessionId + "/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.totalAmount").value(75000));
    }
}
