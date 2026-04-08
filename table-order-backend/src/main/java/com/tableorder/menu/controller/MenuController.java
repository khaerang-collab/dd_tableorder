package com.tableorder.menu.controller;

import com.tableorder.menu.dto.*;
import com.tableorder.menu.service.ImageService;
import com.tableorder.menu.service.MenuService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    private final ImageService imageService;

    @GetMapping("/api/stores/{storeId}/menus")
    public ResponseEntity<List<MenuCategoryResponse>> getMenus(@PathVariable Long storeId) {
        return ResponseEntity.ok(menuService.getMenusForCustomer(storeId));
    }

    @PostMapping("/api/admin/stores/{storeId}/menus")
    public ResponseEntity<MenuResponse> createMenu(@PathVariable Long storeId,
                                                    @Valid @RequestBody CreateMenuRequest request) {
        return ResponseEntity.ok(menuService.createMenu(storeId, request));
    }

    @PutMapping("/api/admin/menus/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(@PathVariable Long menuId,
                                                    @AuthenticationPrincipal Claims claims,
                                                    @Valid @RequestBody CreateMenuRequest request) {
        Long storeId = claims.get("storeId", Long.class);
        return ResponseEntity.ok(menuService.updateMenu(menuId, storeId, request));
    }

    @DeleteMapping("/api/admin/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long menuId,
                                            @AuthenticationPrincipal Claims claims) {
        Long storeId = claims.get("storeId", Long.class);
        menuService.deleteMenu(menuId, storeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/stores/{storeId}/images")
    public ResponseEntity<Map<String, String>> uploadImage(@PathVariable Long storeId,
                                                            @RequestParam("file") MultipartFile file) throws IOException {
        String url = imageService.upload(file);
        return ResponseEntity.ok(Map.of("imageUrl", url));
    }
}
