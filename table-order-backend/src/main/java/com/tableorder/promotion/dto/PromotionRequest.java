package com.tableorder.promotion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PromotionRequest(
        @NotNull @Min(1) Integer minOrderAmount,
        @NotBlank @Size(max = 200) String rewardDescription,
        Boolean isActive
) {}
