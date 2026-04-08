package com.tableorder.menu.controller;

import com.tableorder.menu.entity.Category;
import com.tableorder.menu.entity.Menu;
import com.tableorder.store.entity.Store;
import com.tableorder.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MenuControllerTest extends AbstractIntegrationTest {

    private Store store;
    private Category category;
    private String adminToken;

    @BeforeEach
    void setUp() {
        store = dataFactory.createStore("테스트 매장");
        dataFactory.createAdminUser(store.getId(), "admin", "admin1234");
        category = dataFactory.createCategory(store, "추천 메뉴", 0);
        adminToken = generateAdminToken(store.getId());
    }

    // IT-MENU-001: 공개 메뉴 조회 (인증 불필요)
    @Test
    @DisplayName("GET /api/stores/{storeId}/menus - 인증 없이 메뉴 조회 가능")
    void getMenusPublic() throws Exception {
        dataFactory.createMenu(category, "불고기", 15000);
        dataFactory.createMenu(category, "비빔밥", 10000);
        dataFactory.createUnavailableMenu(category, "품절 메뉴", 8000);

        mockMvc.perform(get("/api/stores/" + store.getId() + "/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // 1개 카테고리
                .andExpect(jsonPath("$[0].menus", hasSize(2))); // available만 2개
    }

    // IT-MENU-002: 메뉴 생성
    @Test
    @DisplayName("POST /api/admin/stores/{storeId}/menus - 메뉴 생성 성공")
    void createMenu() throws Exception {
        String body = """
            {"categoryId": %d, "name": "새 메뉴", "price": 12000, "description": "맛있는 메뉴"}
            """.formatted(category.getId());

        mockMvc.perform(post("/api/admin/stores/" + store.getId() + "/menus")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새 메뉴"))
                .andExpect(jsonPath("$.price").value(12000));
    }

    // IT-MENU-003: 메뉴 생성 유효성 실패
    @Test
    @DisplayName("POST /api/admin/stores/{storeId}/menus - 유효하지 않은 입력 시 400")
    void createMenuValidationFail() throws Exception {
        String body = """
            {"categoryId": null, "name": "", "price": -1}
            """;

        mockMvc.perform(post("/api/admin/stores/" + store.getId() + "/menus")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    // IT-MENU-004: 메뉴 수정
    @Test
    @DisplayName("PUT /api/admin/menus/{menuId} - 메뉴 수정 성공")
    void updateMenu() throws Exception {
        Menu menu = dataFactory.createMenu(category, "기존 메뉴", 10000);

        String body = """
            {"categoryId": %d, "name": "수정된 메뉴", "price": 15000}
            """.formatted(category.getId());

        mockMvc.perform(put("/api/admin/menus/" + menu.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된 메뉴"))
                .andExpect(jsonPath("$.price").value(15000));
    }

    // IT-MENU-005: 메뉴 삭제
    @Test
    @DisplayName("DELETE /api/admin/menus/{menuId} - 메뉴 삭제 성공")
    void deleteMenu() throws Exception {
        Menu menu = dataFactory.createMenu(category, "삭제할 메뉴", 8000);

        mockMvc.perform(delete("/api/admin/menus/" + menu.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
