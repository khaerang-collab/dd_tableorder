package com.tableorder.integration;

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

class MultiTenantIsolationTest extends AbstractIntegrationTest {

    private Store store1, store2;
    private String admin1Token, admin2Token;
    private Category store2Category;
    private Menu store2Menu;

    @BeforeEach
    void setUp() {
        store1 = dataFactory.createStore("매장1");
        store2 = dataFactory.createStore("매장2");
        dataFactory.createAdminUser(store1.getId(), "admin1", "admin1234");
        dataFactory.createAdminUser(store2.getId(), "admin2", "admin1234");
        admin1Token = generateAdminToken(store1.getId());
        admin2Token = generateAdminToken(store2.getId());

        store2Category = dataFactory.createCategory(store2, "매장2 카테고리", 0);
        store2Menu = dataFactory.createMenu(store2Category, "매장2 메뉴", 10000);
    }

    // E2E-MT-001: Store1 Admin이 Store2 데이터 접근 불가
    @Test
    @DisplayName("멀티테넌트: Store1 Admin은 Store2의 카테고리/메뉴에 접근할 수 없다")
    void store1CannotAccessStore2Data() throws Exception {
        // Store2의 카테고리 수정 시도
        mockMvc.perform(put("/api/admin/stores/" + store2.getId() + "/categories/" + store2Category.getId())
                        .header("Authorization", "Bearer " + admin1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"해킹 시도\"}"))
                .andExpect(status().is4xxClientError());

        // Store2의 메뉴 삭제 시도
        mockMvc.perform(delete("/api/admin/menus/" + store2Menu.getId())
                        .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().is4xxClientError());
    }

    // E2E-MT-002: Store별 대시보드 격리
    @Test
    @DisplayName("멀티테넌트: 각 매장의 대시보드에는 자신의 주문만 표시된다")
    void dashboardIsolation() throws Exception {
        // Store1 주문 3건
        Category cat1 = dataFactory.createCategory(store1, "매장1 카테고리", 0);
        Menu menu1 = dataFactory.createMenu(cat1, "매장1 메뉴", 15000);
        RestaurantTable table1 = dataFactory.createTable(store1, 1);
        TableSession session1 = dataFactory.createActiveSession(table1);
        for (int i = 0; i < 3; i++) {
            OrderItem item = dataFactory.buildOrderItem(menu1.getId(), "매장1 메뉴", 1, 15000);
            dataFactory.createOrder(session1.getId(), "PENDING", 15000, List.of(item));
        }

        // Store2 주문 2건
        RestaurantTable table2 = dataFactory.createTable(store2, 1);
        TableSession session2 = dataFactory.createActiveSession(table2);
        for (int i = 0; i < 2; i++) {
            OrderItem item = dataFactory.buildOrderItem(store2Menu.getId(), "매장2 메뉴", 1, 10000);
            dataFactory.createOrder(session2.getId(), "PENDING", 10000, List.of(item));
        }

        // Store1 대시보드: 자신의 데이터만
        mockMvc.perform(get("/api/admin/stores/" + store1.getId() + "/dashboard")
                        .header("Authorization", "Bearer " + admin1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tables[0].orderCount").value(3));

        // Store2 대시보드: 자신의 데이터만
        mockMvc.perform(get("/api/admin/stores/" + store2.getId() + "/dashboard")
                        .header("Authorization", "Bearer " + admin2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tables[0].orderCount").value(2));
    }
}
