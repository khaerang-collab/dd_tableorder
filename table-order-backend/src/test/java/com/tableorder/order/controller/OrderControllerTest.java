package com.tableorder.order.controller;

import com.tableorder.cart.entity.Cart;
import com.tableorder.customer.entity.CustomerProfile;
import com.tableorder.menu.entity.Category;
import com.tableorder.menu.entity.Menu;
import com.tableorder.order.entity.Order;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest extends AbstractIntegrationTest {

    private Store store;
    private TableSession session;
    private Menu menu;
    private String customerToken;
    private String adminToken;
    private CustomerProfile profile;

    @BeforeEach
    void setUp() {
        store = dataFactory.createStore("테스트 매장");
        dataFactory.createAdminUser(store.getId(), "admin", "admin1234");
        Category category = dataFactory.createCategory(store, "메인", 0);
        menu = dataFactory.createMenu(category, "불고기", 15000);
        RestaurantTable table = dataFactory.createTable(store, 1);
        session = dataFactory.createActiveSession(table);
        profile = dataFactory.createCustomerProfile(12345L, "테스터");
        customerToken = generateCustomerToken(store.getId(), table.getId(), session.getId(), profile.getId());
        adminToken = generateAdminToken(store.getId());
    }

    // IT-ORD-001: 주문 생성 성공
    @Test
    @DisplayName("POST /api/customer/sessions/{id}/orders - 주문 생성 성공")
    void createOrder() throws Exception {
        Cart cart = dataFactory.createCart(session.getId());
        dataFactory.createCartItemWithProfile(cart, menu, profile.getId(), "test-device-" + profile.getId(), 2);

        mockMvc.perform(post("/api/customer/sessions/" + session.getId() + "/orders")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(30000))
                .andExpect(jsonPath("$.orderNumber").isNotEmpty());
    }

    // IT-ORD-002: 빈 장바구니 주문
    @Test
    @DisplayName("POST orders - 빈 장바구니 시 400 EMPTY_CART")
    void createOrderEmptyCart() throws Exception {
        mockMvc.perform(post("/api/customer/sessions/" + session.getId() + "/orders")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMPTY_CART"));
    }

    // IT-ORD-003: 주문 목록 조회
    @Test
    @DisplayName("GET /api/customer/sessions/{id}/orders - 주문 목록 조회")
    void getOrders() throws Exception {
        OrderItem item = dataFactory.buildOrderItem(menu.getId(), "불고기", 2, 15000);
        dataFactory.createOrder(session.getId(), "PENDING", 30000, List.of(item));

        mockMvc.perform(get("/api/customer/sessions/" + session.getId() + "/orders")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    // IT-ORD-004: 주문 상태 변경 PENDING → PREPARING
    @Test
    @DisplayName("PUT /api/admin/orders/{id}/status - PENDING→PREPARING 성공")
    void updateStatusToPreparing() throws Exception {
        OrderItem item = dataFactory.buildOrderItem(menu.getId(), "불고기", 1, 15000);
        Order order = dataFactory.createOrder(session.getId(), "PENDING", 15000, List.of(item));

        mockMvc.perform(put("/api/admin/orders/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"PREPARING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));
    }

    // IT-ORD-005: 잘못된 상태 전이
    @Test
    @DisplayName("PUT orders/status - PENDING→COMPLETED는 400")
    void invalidStatusTransition() throws Exception {
        OrderItem item = dataFactory.buildOrderItem(menu.getId(), "불고기", 1, 15000);
        Order order = dataFactory.createOrder(session.getId(), "PENDING", 15000, List.of(item));

        mockMvc.perform(put("/api/admin/orders/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"COMPLETED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
    }

    // IT-ORD-006: 주문 삭제
    @Test
    @DisplayName("DELETE /api/admin/orders/{id} - 주문 삭제 성공")
    void deleteOrder() throws Exception {
        OrderItem item = dataFactory.buildOrderItem(menu.getId(), "불고기", 1, 15000);
        Order order = dataFactory.createOrder(session.getId(), "PENDING", 15000, List.of(item));

        mockMvc.perform(delete("/api/admin/orders/" + order.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // IT-ORD-007: 대시보드 조회
    @Test
    @DisplayName("GET /api/admin/stores/{id}/dashboard - 대시보드 조회 성공")
    void getDashboard() throws Exception {
        OrderItem item = dataFactory.buildOrderItem(menu.getId(), "불고기", 2, 15000);
        dataFactory.createOrder(session.getId(), "PENDING", 30000, List.of(item));

        mockMvc.perform(get("/api/admin/stores/" + store.getId() + "/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tables").isArray())
                .andExpect(jsonPath("$.tables[0].tableNumber").value(1));
    }
}
