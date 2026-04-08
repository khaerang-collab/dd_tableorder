package com.tableorder.promotion.dto;

public record PromotionNudgeResponse(
        int minOrderAmount,
        String rewardDescription,
        int currentAmount,
        int remainingAmount,
        boolean achieved
) {}
