package com.tableorder.menu.service;

import com.tableorder.common.exception.ForbiddenException;
import com.tableorder.common.exception.NotFoundException;
import com.tableorder.menu.dto.CreateMenuRequest;
import com.tableorder.menu.dto.MenuCategoryResponse;
import com.tableorder.menu.dto.MenuResponse;
import com.tableorder.menu.entity.Category;
import com.tableorder.menu.entity.Menu;
import com.tableorder.menu.repository.CategoryRepository;
import com.tableorder.menu.repository.MenuRepository;
import com.tableorder.store.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @InjectMocks
    private MenuService menuService;

    @Mock private MenuRepository menuRepository;
    @Mock private CategoryRepository categoryRepository;

    private Store store;
    private Category category;

    @BeforeEach
    void setUp() {
        store = Store.builder().id(1L).name("테스트 매장").build();
        category = Category.builder().id(1L).store(store).name("추천 메뉴").displayOrder(0).build();
    }

    // UT-MENU-001: 고객용 메뉴 조회 (available만)
    @Test
    @DisplayName("고객에게는 is_available=true인 메뉴만 카테고리별로 반환된다")
    void getMenusForCustomerOnlyAvailable() {
        Menu m1 = Menu.builder().id(1L).category(category).name("불고기").price(15000).isAvailable(true).build();
        Menu m2 = Menu.builder().id(2L).category(category).name("비빔밥").price(10000).isAvailable(true).build();

        when(menuRepository.findAvailableByStoreId(1L)).thenReturn(List.of(m1, m2));

        List<MenuCategoryResponse> result = menuService.getMenusForCustomer(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).categoryName()).isEqualTo("추천 메뉴");
        assertThat(result.get(0).menus()).hasSize(2);
    }

    // UT-MENU-002: 메뉴 생성
    @Test
    @DisplayName("유효한 요청으로 메뉴를 생성할 수 있다")
    void createMenu() {
        CreateMenuRequest req = new CreateMenuRequest(1L, "새 메뉴", 12000, "설명", null, "NEW");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(menuRepository.countByCategoryId(1L)).thenReturn(2);
        when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> {
            Menu m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });

        MenuResponse result = menuService.createMenu(1L, req);

        assertThat(result.name()).isEqualTo("새 메뉴");
        assertThat(result.price()).isEqualTo(12000);
        assertThat(result.displayOrder()).isEqualTo(2);
    }

    // UT-MENU-003: 메뉴 수정
    @Test
    @DisplayName("기존 메뉴의 이름과 가격을 수정할 수 있다")
    void updateMenu() {
        Menu existing = Menu.builder().id(1L).category(category).name("기존 메뉴").price(10000).build();
        CreateMenuRequest req = new CreateMenuRequest(1L, "수정된 메뉴", 15000, "새 설명", null, null);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(existing));

        MenuResponse result = menuService.updateMenu(1L, 1L, req);

        assertThat(result.name()).isEqualTo("수정된 메뉴");
        assertThat(result.price()).isEqualTo(15000);
    }

    // UT-MENU-004: 메뉴 삭제
    @Test
    @DisplayName("자신의 매장 메뉴를 삭제할 수 있다")
    void deleteMenu() {
        Menu existing = Menu.builder().id(1L).category(category).name("삭제할 메뉴").build();
        when(menuRepository.findById(1L)).thenReturn(Optional.of(existing));

        menuService.deleteMenu(1L, 1L);

        verify(menuRepository).delete(existing);
    }

    // UT-MENU-005: 다른 매장의 메뉴 수정 시도
    @Test
    @DisplayName("다른 매장의 메뉴를 수정하면 ForbiddenException이 발생한다")
    void updateOtherStoreMenu() {
        Store otherStore = Store.builder().id(2L).build();
        Category otherCategory = Category.builder().id(2L).store(otherStore).build();
        Menu otherMenu = Menu.builder().id(1L).category(otherCategory).name("다른 매장 메뉴").build();
        CreateMenuRequest req = new CreateMenuRequest(2L, "수정 시도", 9999, null, null, null);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(otherMenu));

        assertThatThrownBy(() -> menuService.updateMenu(1L, 1L, req))
                .isInstanceOf(ForbiddenException.class);
    }

    // UT-MENU-006: 존재하지 않는 카테고리에 메뉴 생성
    @Test
    @DisplayName("존재하지 않는 카테고리에 메뉴를 생성하면 NotFoundException이 발생한다")
    void createMenuCategoryNotFound() {
        CreateMenuRequest req = new CreateMenuRequest(999L, "메뉴", 10000, null, null, null);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.createMenu(1L, req))
                .isInstanceOf(NotFoundException.class);
    }

    // UT-MENU-007: 메뉴 없는 카테고리는 고객 조회에서 제외
    @Test
    @DisplayName("available 메뉴가 없는 카테고리는 고객 조회 결과에서 제외된다")
    void emptyCategoryExcluded() {
        when(menuRepository.findAvailableByStoreId(1L)).thenReturn(List.of());

        List<MenuCategoryResponse> result = menuService.getMenusForCustomer(1L);

        assertThat(result).isEmpty();
    }
}
