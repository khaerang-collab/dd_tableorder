package com.tableorder.menu.service;

import com.tableorder.common.exception.CategoryHasMenusException;
import com.tableorder.common.exception.ForbiddenException;
import com.tableorder.common.exception.NotFoundException;
import com.tableorder.menu.dto.CategoryResponse;
import com.tableorder.menu.entity.Category;
import com.tableorder.menu.repository.CategoryRepository;
import com.tableorder.menu.repository.MenuRepository;
import com.tableorder.store.entity.Store;
import com.tableorder.store.service.StoreService;
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
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock private CategoryRepository categoryRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private StoreService storeService;

    private Store store;

    @BeforeEach
    void setUp() {
        store = Store.builder().id(1L).name("테스트 매장").build();
    }

    // UT-CAT-001: 카테고리 목록 조회
    @Test
    @DisplayName("매장의 카테고리를 displayOrder 순서로 조회한다")
    void getCategories() {
        Category c1 = Category.builder().id(1L).store(store).name("추천").displayOrder(0).build();
        Category c2 = Category.builder().id(2L).store(store).name("메인").displayOrder(1).build();
        Category c3 = Category.builder().id(3L).store(store).name("음료").displayOrder(2).build();

        when(categoryRepository.findByStoreIdOrderByDisplayOrder(1L)).thenReturn(List.of(c1, c2, c3));

        List<CategoryResponse> result = categoryService.getCategories(1L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("추천");
        assertThat(result.get(2).name()).isEqualTo("음료");
    }

    // UT-CAT-002: 카테고리 생성
    @Test
    @DisplayName("새 카테고리를 생성하면 displayOrder가 자동 할당된다")
    void createCategory() {
        when(storeService.getStore(1L)).thenReturn(store);
        when(categoryRepository.countByStoreId(1L)).thenReturn(2);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(4L);
            return c;
        });

        CategoryResponse result = categoryService.createCategory(1L, "새 카테고리");

        assertThat(result.name()).isEqualTo("새 카테고리");
        assertThat(result.displayOrder()).isEqualTo(2);
    }

    // UT-CAT-003: 카테고리 수정
    @Test
    @DisplayName("카테고리 이름을 수정할 수 있다")
    void updateCategory() {
        Category c = Category.builder().id(1L).store(store).name("기존 이름").displayOrder(0).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));

        CategoryResponse result = categoryService.updateCategory(1L, 1L, "수정된 이름");

        assertThat(result.name()).isEqualTo("수정된 이름");
    }

    // UT-CAT-004: 메뉴가 있는 카테고리 삭제 시도
    @Test
    @DisplayName("소속 메뉴가 있는 카테고리를 삭제하면 CategoryHasMenusException이 발생한다")
    void deleteCategoryWithMenus() {
        Category c = Category.builder().id(1L).store(store).name("메인").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));
        when(menuRepository.countByCategoryId(1L)).thenReturn(3);

        assertThatThrownBy(() -> categoryService.deleteCategory(1L, 1L))
                .isInstanceOf(CategoryHasMenusException.class);
    }

    // UT-CAT-005: 빈 카테고리 삭제
    @Test
    @DisplayName("소속 메뉴가 없는 카테고리는 정상적으로 삭제된다")
    void deleteEmptyCategory() {
        Category c = Category.builder().id(1L).store(store).name("빈 카테고리").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));
        when(menuRepository.countByCategoryId(1L)).thenReturn(0);

        categoryService.deleteCategory(1L, 1L);

        verify(categoryRepository).delete(c);
    }

    // UT-CAT-006: 다른 매장의 카테고리 접근
    @Test
    @DisplayName("다른 매장의 카테고리를 수정하면 ForbiddenException이 발생한다")
    void accessOtherStoreCategory() {
        Store otherStore = Store.builder().id(2L).name("다른 매장").build();
        Category c = Category.builder().id(1L).store(otherStore).name("다른 매장 카테고리").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> categoryService.updateCategory(1L, 1L, "악의적 수정"))
                .isInstanceOf(ForbiddenException.class);
    }

    // UT-CAT-007: 카테고리 순서 변경
    @Test
    @DisplayName("카테고리 순서를 변경하면 displayOrder가 업데이트된다")
    void reorderCategories() {
        Category c1 = Category.builder().id(1L).store(store).name("A").displayOrder(0).build();
        Category c2 = Category.builder().id(2L).store(store).name("B").displayOrder(1).build();
        Category c3 = Category.builder().id(3L).store(store).name("C").displayOrder(2).build();

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(c3));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c1));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(c2));

        categoryService.reorderCategories(1L, List.of(3L, 1L, 2L));

        assertThat(c3.getDisplayOrder()).isEqualTo(0);
        assertThat(c1.getDisplayOrder()).isEqualTo(1);
        assertThat(c2.getDisplayOrder()).isEqualTo(2);
    }
}
