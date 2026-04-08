package com.tableorder.menu.dto;

import jakarta.validation.constraints.*;

public record CreateMenuRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 100) String name,
        @NotNull @Min(0) @Max(10000000) Integer price,
        @Size(max = 500) String description,
        String imageUrl,
        String badgeType
) {}
