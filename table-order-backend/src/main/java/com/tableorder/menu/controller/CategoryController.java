package com.tableorder.menu.controller;

import com.tableorder.menu.dto.CategoryRequest;
import com.tableorder.menu.dto.CategoryResponse;
import com.tableorder.menu.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stores/{storeId}/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(@PathVariable Long storeId) {
        return ResponseEntity.ok(categoryService.getCategories(storeId));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@PathVariable Long storeId,
                                                    @Valid @RequestBody CategoryRequest req) {
        return ResponseEntity.ok(categoryService.createCategory(storeId, req.name()));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long storeId, @PathVariable Long categoryId,
                                                    @Valid @RequestBody CategoryRequest req) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, storeId, req.name()));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable Long storeId, @PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId, storeId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorder(@PathVariable Long storeId, @RequestBody List<Long> orderedIds) {
        categoryService.reorderCategories(storeId, orderedIds);
        return ResponseEntity.noContent().build();
    }
}
