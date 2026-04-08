package com.tableorder.table.controller;

import com.tableorder.cart.entity.Cart;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TableControllerTest extends AbstractIntegrationTest {

    private Store store;
    private String adminToken;

    @BeforeEach
    void setUp() {
        store = dataFactory.createStore("테스트 매장");
        dataFactory.createAdminUser(store.getId(), "admin", "admin1234");
        adminToken = generateAdminToken(store.getId());
    }

    // IT-TBL-001: 테이블 생성
    @Test
    @DisplayName("POST /api/admin/stores/{id}/tables - 테이블 생성 성공")
    void createTable() throws Exception {
        String body = """
            {"tableNumber": 10, "baseUrl": "http://example.com"}
            """;

        mockMvc.perform(post("/api/admin/stores/" + store.getId() + "/tables")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tableNumber").value(10))
                .andExpect(jsonPath("$.qrCodeUrl").value("http://example.com/customer?storeId=" + store.getId() + "&table=10"));
    }

    // IT-TBL-002: 테이블 목록 조회
    @Test
    @DisplayName("GET /api/admin/stores/{id}/tables - 테이블 목록 조회")
    void getTables() throws Exception {
        dataFactory.createTable(store, 1);
        dataFactory.createTable(store, 2);
        dataFactory.createTable(store, 3);

        mockMvc.perform(get("/api/admin/stores/" + store.getId() + "/tables")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    // IT-TBL-003: 세션 완료
    @Test
    @DisplayName("POST /api/admin/stores/{id}/tables/{tableId}/complete - 세션 완료 성공")
    void completeSession() throws Exception {
        RestaurantTable table = dataFactory.createTable(store, 1);
        TableSession session = dataFactory.createActiveSession(table);
        Category cat = dataFactory.createCategory(store, "메인", 0);
        Menu menu = dataFactory.createMenu(cat, "불고기", 15000);
        OrderItem item = dataFactory.buildOrderItem(menu.getId(), "불고기", 1, 15000);
        dataFactory.createOrder(session.getId(), "COMPLETED", 15000, List.of(item));

        mockMvc.perform(post("/api/admin/stores/" + store.getId() + "/tables/" + table.getId() + "/complete")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // IT-TBL-004: 활성 세션 없는 테이블 완료 시도
    @Test
    @DisplayName("POST tables/{id}/complete - 활성 세션 없으면 404")
    void completeSessionNotFound() throws Exception {
        RestaurantTable table = dataFactory.createTable(store, 1);

        mockMvc.perform(post("/api/admin/stores/" + store.getId() + "/tables/" + table.getId() + "/complete")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
