package com.tableorder.recommend.controller;

import com.tableorder.recommend.dto.MenuRecommendResponse;
import com.tableorder.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    // 메뉴 상세 페이지: 페어링 추천
    @GetMapping("/api/stores/{storeId}/menus/{menuId}/pairings")
    public ResponseEntity<List<MenuRecommendResponse>> getPairings(@PathVariable Long storeId,
                                                                     @PathVariable Long menuId) {
        return ResponseEntity.ok(recommendService.getPairings(menuId));
    }
}
