package com.tableorder.menu.dto;

import java.util.List;

public record MenuCategoryResponse(Long categoryId, String categoryName, int displayOrder, List<MenuResponse> menus) {}
