package com.tableorder.integration;

import com.tableorder.cart.entity.Cart;
import com.tableorder.customer.entity.CustomerProfile;
import com.tableorder.menu.entity.Category;
import com.tableorder.menu.entity.Menu;
import com.tableorder.order.entity.OrderItem;
import com.tableorder.store.entity.Store;
import com.tableorder.support.AbstractIntegrationTest;
import com.tableorder.table.entity.RestaurantTable;
import com.tableorder.table.entity.TableSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminManagementFlowTest extends AbstractIntegrationTest {

    private Store store;
    private String adminToken;

    @BeforeEach
    void setUp() {
        store = dataFactory.createStore("테스트 매장");
        dataFactory.createAdminUser(store.getId(), "admin", "admin1234");
        adminToken = generateAdminToken(store.getId());
    }

    // E2E-ADMIN-001: 관리자 전체 플로우
    @Test
    @DisplayName("E2E: 로그인 → 대시보드 → 주문 상태 변경 → 이용 완료")
    void fullAdminFlow() throws Exception {
        // 테스트 데이터 준비
        Category cat = dataFactory.createCategory(store, "메인", 0);
        Menu menu = dataFactory.createMenu(cat, "불고기", 15000);
        RestaurantTable table = dataFactory.createTable(store, 1);
        TableSession session = dataFactory.createActiveSession(table);
        OrderItem item = dataFactory.buildOrderItem(menu.getId(), "불고기", 2, 15000);
        dataFactory.createOrder(session.getId(), "PENDING", 30000, List.of(item));

        // 1. 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storeId\": " + store.getId() + ", \"username\": \"admin\", \"password\": \"admin1234\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // 2. 대시보드 조회
        mockMvc.perform(get("/api/admin/stores/" + store.getId() + "/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tables", hasSize(1)))
                .andExpect(jsonPath("$.tables[0].totalAmount").value(30000))
                .andExpect(jsonPath("$.tables[0].orderCount").value(1));

        // 3. 주문 상태 변경: PENDING → PREPARING
        MvcResult dashResult = mockMvc.perform(get("/api/admin/stores/" + store.getId() + "/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andReturn();

        long orderId = objectMapper.readTree(dashResult.getResponse().getContentAsString())
                .get("tables").get(0).get("recentOrders").get(0).get("id").asLong();

        mockMvc.perform(put("/api/admin/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"PREPARING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));

        // 4. 주문 상태 변경: PREPARING → COMPLETED
        mockMvc.perform(put("/api/admin/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 5. 이용 완료
        mockMvc.perform(post("/api/admin/stores/" + store.getId() + "/tables/" + table.getId() + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 6. 히스토리 확인
        mockMvc.perform(get("/api/admin/stores/" + store.getId() + "/orders/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
