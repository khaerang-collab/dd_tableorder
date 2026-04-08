package com.tableorder.menu.controller;

import com.tableorder.menu.entity.Category;
import com.tableorder.store.entity.Store;
import com.tableorder.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryControllerTest extends AbstractIntegrationTest {

    private Store store;
    private String adminToken;

    @BeforeEach
    void setUp() {
        store = dataFactory.createStore("테스트 매장");
        dataFactory.createAdminUser(store.getId(), "admin", "admin1234");
        adminToken = generateAdminToken(store.getId());
    }

    // IT-MENU-006: 카테고리 생성
    @Test
    @DisplayName("POST /api/admin/stores/{storeId}/categories - 카테고리 생성 성공")
    void createCategory() throws Exception {
        mockMvc.perform(post("/api/admin/stores/" + store.getId() + "/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"새 카테고리\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새 카테고리"))
                .andExpect(jsonPath("$.displayOrder").isNumber());
    }

    // IT-MENU-007: 메뉴 있는 카테고리 삭제
    @Test
    @DisplayName("DELETE /api/admin/stores/{storeId}/categories/{id} - 메뉴 있으면 400")
    void deleteCategoryWithMenus() throws Exception {
        Category cat = dataFactory.createCategory(store, "삭제 테스트", 0);
        dataFactory.createMenu(cat, "메뉴1", 10000);

        mockMvc.perform(delete("/api/admin/stores/" + store.getId() + "/categories/" + cat.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CATEGORY_HAS_MENUS"));
    }

    // IT-MENU-008 (추가): 빈 카테고리 삭제 성공
    @Test
    @DisplayName("DELETE /api/admin/stores/{storeId}/categories/{id} - 빈 카테고리 삭제 성공")
    void deleteEmptyCategory() throws Exception {
        Category cat = dataFactory.createCategory(store, "빈 카테고리", 0);

        mockMvc.perform(delete("/api/admin/stores/" + store.getId() + "/categories/" + cat.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
