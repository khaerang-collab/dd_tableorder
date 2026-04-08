package com.tableorder.promotion.controller;

import com.tableorder.promotion.dto.PromotionNudgeResponse;
import com.tableorder.promotion.dto.PromotionRequest;
import com.tableorder.promotion.dto.PromotionResponse;
import com.tableorder.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    // 관리자: 프로모션 CRUD
    @GetMapping("/api/admin/stores/{storeId}/promotions")
    public ResponseEntity<List<PromotionResponse>> getPromotions(@PathVariable Long storeId) {
        return ResponseEntity.ok(promotionService.getPromotions(storeId));
    }

    @PostMapping("/api/admin/stores/{storeId}/promotions")
    public ResponseEntity<PromotionResponse> createPromotion(@PathVariable Long storeId,
                                                              @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(promotionService.createPromotion(storeId, request));
    }

    @PutMapping("/api/admin/promotions/{promotionId}")
    public ResponseEntity<PromotionResponse> updatePromotion(@PathVariable Long promotionId,
                                                              @RequestParam Long storeId,
                                                              @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(promotionService.updatePromotion(promotionId, storeId, request));
    }

    @PostMapping("/api/admin/promotions/{promotionId}/toggle")
    public ResponseEntity<Map<String, Boolean>> togglePromotion(@PathVariable Long promotionId,
                                                                 @RequestParam Long storeId) {
        promotionService.togglePromotion(promotionId, storeId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/api/admin/promotions/{promotionId}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long promotionId, @RequestParam Long storeId) {
        promotionService.deletePromotion(promotionId, storeId);
        return ResponseEntity.noContent().build();
    }

    // 고객: 프로모션 넛지 조회
    @GetMapping("/api/stores/{storeId}/promotions/nudge")
    public ResponseEntity<List<PromotionNudgeResponse>> getNudges(@PathVariable Long storeId,
                                                                    @RequestParam(defaultValue = "0") int cartAmount) {
        return ResponseEntity.ok(promotionService.getNudges(storeId, cartAmount));
    }
}
