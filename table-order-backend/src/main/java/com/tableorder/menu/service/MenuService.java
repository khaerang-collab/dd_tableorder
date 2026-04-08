package com.tableorder.menu.service;

import com.tableorder.common.exception.ForbiddenException;
import com.tableorder.common.exception.NotFoundException;
import com.tableorder.menu.dto.*;
import com.tableorder.menu.entity.Category;
import com.tableorder.menu.entity.Menu;
import com.tableorder.menu.repository.CategoryRepository;
import com.tableorder.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;

    public List<MenuCategoryResponse> getMenusForCustomer(Long storeId) {
        List<Category> categories = categoryRepository.findByStoreIdOrderByDisplayOrder(storeId);
        List<Menu> menus = menuRepository.findAvailableByStoreId(storeId);
        var menusByCategory = menus.stream().collect(Collectors.groupingBy(m -> m.getCategory().getId()));

        return categories.stream()
                .map(c -> new MenuCategoryResponse(c.getId(), c.getName(), c.getDisplayOrder(),
                        menusByCategory.getOrDefault(c.getId(), List.of()).stream()
                                .map(this::toResponse).toList()))
                .filter(c -> !c.menus().isEmpty())
                .toList();
    }

    @Transactional
    public MenuResponse createMenu(Long storeId, CreateMenuRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다"));
        if (!category.getStore().getId().equals(storeId)) throw new ForbiddenException("접근 권한이 없습니다");

        int order = menuRepository.countByCategoryId(req.categoryId());
        Menu menu = Menu.builder()
                .category(category).name(req.name()).price(req.price())
                .description(req.description()).imageUrl(req.imageUrl())
                .badgeType(req.badgeType()).displayOrder(order).build();
        return toResponse(menuRepository.save(menu));
    }

    @Transactional
    public MenuResponse updateMenu(Long menuId, Long storeId, CreateMenuRequest req) {
        Menu menu = getMenuAndValidate(menuId, storeId);
        menu.setName(req.name());
        menu.setPrice(req.price());
        menu.setDescription(req.description());
        if (req.imageUrl() != null) menu.setImageUrl(req.imageUrl());
        if (req.badgeType() != null) menu.setBadgeType(req.badgeType());
        return toResponse(menu);
    }

    @Transactional
    public void deleteMenu(Long menuId, Long storeId) {
        Menu menu = getMenuAndValidate(menuId, storeId);
        menuRepository.delete(menu);
    }

    private Menu getMenuAndValidate(Long menuId, Long storeId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("메뉴를 찾을 수 없습니다"));
        if (!menu.getCategory().getStore().getId().equals(storeId)) throw new ForbiddenException("접근 권한이 없습니다");
        return menu;
    }

    private MenuResponse toResponse(Menu m) {
        return new MenuResponse(m.getId(), m.getCategory().getId(), m.getName(), m.getPrice(),
                m.getDescription(), m.getImageUrl(), m.getBadgeType(), m.getDisplayOrder(), m.isAvailable());
    }
}
