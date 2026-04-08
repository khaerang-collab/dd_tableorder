package com.tableorder.cart.controller;

import com.tableorder.cart.entity.Cart;
import com.tableorder.cart.entity.CartItem;
import com.tableorder.customer.entity.CustomerProfile;
import com.tableorder.menu.entity.Category;
import com.tableorder.menu.entity.Menu;
import com.tableorder.store.entity.Store;
import com.tableorder.support.AbstractIntegrationTest;
import com.tableorder.table.entity.RestaurantTable;
import com.tableorder.table.entity.TableSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerTest extends AbstractIntegrationTest {

    private Store store;
    private TableSession session;
    private Menu menu;
    private String customerToken;
    private CustomerProfile profile;

    @BeforeEach
    void setUp() {
        store = dataFactory.createStore("테스트 매장");
        Category category = dataFactory.createCategory(store, "메인", 0);
        menu = dataFactory.createMenu(category, "불고기", 15000);
        RestaurantTable table = dataFactory.createTable(store, 1);
        session = dataFactory.createActiveSession(table);
        profile = dataFactory.createCustomerProfile(12345L, "테스터");
        customerToken = generateCustomerToken(store.getId(), table.getId(), session.getId(), profile.getId());
    }

    // IT-CART-001: 빈 장바구니 조회
    @Test
    @DisplayName("GET /api/customer/sessions/{id}/cart - 빈 장바구니 조회")
    void getEmptyCart() throws Exception {
        mockMvc.perform(get("/api/customer/sessions/" + session.getId() + "/cart")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalAmount").value(0));
    }

    // IT-CART-002: 장바구니 아이템 추가
    @Test
    @DisplayName("POST /api/customer/sessions/{id}/cart/items - 아이템 추가 성공")
    void addCartItem() throws Exception {
        String body = """
            {"menuId": %d, "quantity": 2}
            """.formatted(menu.getId());

        mockMvc.perform(post("/api/customer/sessions/" + session.getId() + "/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalAmount").value(30000)); // 15000 * 2
    }

    // IT-CART-003: 수량 범위 위반 (0)
    @Test
    @DisplayName("POST cart/items - 수량 0은 400")
    void addCartItemQuantityZero() throws Exception {
        String body = """
            {"menuId": %d, "quantity": 0}
            """.formatted(menu.getId());

        mockMvc.perform(post("/api/customer/sessions/" + session.getId() + "/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // IT-CART-004: 수량 범위 위반 (100)
    @Test
    @DisplayName("POST cart/items - 수량 100은 400")
    void addCartItemQuantityExceedsMax() throws Exception {
        String body = """
            {"menuId": %d, "quantity": 100}
            """.formatted(menu.getId());

        mockMvc.perform(post("/api/customer/sessions/" + session.getId() + "/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // IT-CART-005: 아이템 수량 수정
    @Test
    @DisplayName("PUT cart/items/{itemId} - 수량 수정 성공")
    void updateCartItem() throws Exception {
        Cart cart = dataFactory.createCart(session.getId());
        CartItem item = dataFactory.createCartItemWithProfile(cart, menu, profile.getId(), "test-device-" + profile.getId(), 2);

        mockMvc.perform(put("/api/customer/sessions/" + session.getId() + "/cart/items/" + item.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(75000)); // 15000 * 5
    }

    // IT-CART-006: 아이템 삭제
    @Test
    @DisplayName("DELETE cart/items/{itemId} - 아이템 삭제 성공")
    void removeCartItem() throws Exception {
        Cart cart = dataFactory.createCart(session.getId());
        CartItem item = dataFactory.createCartItemWithProfile(cart, menu, profile.getId(), "test-device-" + profile.getId(), 1);

        mockMvc.perform(delete("/api/customer/sessions/" + session.getId() + "/cart/items/" + item.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }
}
