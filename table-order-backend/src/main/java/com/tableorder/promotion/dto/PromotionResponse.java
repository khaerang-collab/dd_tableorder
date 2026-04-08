package com.tableorder.promotion.dto;

public record PromotionResponse(Long id, int minOrderAmount, String rewardDescription, boolean isActive) {}
