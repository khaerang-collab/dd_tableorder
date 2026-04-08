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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final StoreService storeService;

    public List<CategoryResponse> getCategories(Long storeId) {
        return categoryRepository.findByStoreIdOrderByDisplayOrder(storeId).stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDisplayOrder())).toList();
    }

    @Transactional
    public CategoryResponse createCategory(Long storeId, String name) {
        Store store = storeService.getStore(storeId);
        int order = categoryRepository.countByStoreId(storeId);
        Category cat = categoryRepository.save(Category.builder().store(store).name(name).displayOrder(order).build());
        return new CategoryResponse(cat.getId(), cat.getName(), cat.getDisplayOrder());
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, Long storeId, String name) {
        Category cat = getAndValidate(categoryId, storeId);
        cat.setName(name);
        return new CategoryResponse(cat.getId(), cat.getName(), cat.getDisplayOrder());
    }

    @Transactional
    public void deleteCategory(Long categoryId, Long storeId) {
        Category cat = getAndValidate(categoryId, storeId);
        if (menuRepository.countByCategoryId(categoryId) > 0) throw new CategoryHasMenusException();
        categoryRepository.delete(cat);
    }

    @Transactional
    public void reorderCategories(Long storeId, List<Long> orderedIds) {
        for (int i = 0; i < orderedIds.size(); i++) {
            Category cat = getAndValidate(orderedIds.get(i), storeId);
            cat.setDisplayOrder(i);
        }
    }

    private Category getAndValidate(Long id, Long storeId) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다"));
        if (!cat.getStore().getId().equals(storeId)) throw new ForbiddenException("접근 권한이 없습니다");
        return cat;
    }
}
