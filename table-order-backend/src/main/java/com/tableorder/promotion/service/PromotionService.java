package com.tableorder.promotion.service;

import com.tableorder.common.exception.ForbiddenException;
import com.tableorder.common.exception.NotFoundException;
import com.tableorder.promotion.dto.PromotionNudgeResponse;
import com.tableorder.promotion.dto.PromotionRequest;
import com.tableorder.promotion.dto.PromotionResponse;
import com.tableorder.promotion.entity.Promotion;
import com.tableorder.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository repository;

    public List<PromotionResponse> getPromotions(Long storeId) {
        return repository.findByStoreIdOrderByMinOrderAmountAsc(storeId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public PromotionResponse createPromotion(Long storeId, PromotionRequest request) {
        Promotion p = repository.save(Promotion.builder()
                .storeId(storeId)
                .minOrderAmount(request.minOrderAmount())
                .rewardDescription(request.rewardDescription())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build());
        return toResponse(p);
    }

    @Transactional
    public PromotionResponse updatePromotion(Long promotionId, Long storeId, PromotionRequest request) {
        Promotion p = getAndValidate(promotionId, storeId);
        p.setMinOrderAmount(request.minOrderAmount());
        p.setRewardDescription(request.rewardDescription());
        if (request.isActive() != null) p.setActive(request.isActive());
        return toResponse(p);
    }

    @Transactional
    public void togglePromotion(Long promotionId, Long storeId) {
        Promotion p = getAndValidate(promotionId, storeId);
        p.setActive(!p.isActive());
    }

    @Transactional
    public void deletePromotion(Long promotionId, Long storeId) {
        Promotion p = getAndValidate(promotionId, storeId);
        repository.delete(p);
    }

    // FR-C08: 고객용 넛지 - 현재 장바구니 금액에 대한 프로모션 진행률
    public List<PromotionNudgeResponse> getNudges(Long storeId, int currentCartAmount) {
        return repository.findByStoreIdAndIsActiveTrueOrderByMinOrderAmountAsc(storeId)
                .stream().map(p -> new PromotionNudgeResponse(
                        p.getMinOrderAmount(),
                        p.getRewardDescription(),
                        currentCartAmount,
                        Math.max(0, p.getMinOrderAmount() - currentCartAmount),
                        currentCartAmount >= p.getMinOrderAmount()
                )).toList();
    }

    private Promotion getAndValidate(Long id, Long storeId) {
        Promotion p = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("프로모션을 찾을 수 없습니다"));
        if (!p.getStoreId().equals(storeId)) throw new ForbiddenException("접근 권한이 없습니다");
        return p;
    }

    private PromotionResponse toResponse(Promotion p) {
        return new PromotionResponse(p.getId(), p.getMinOrderAmount(), p.getRewardDescription(), p.isActive());
    }
}
