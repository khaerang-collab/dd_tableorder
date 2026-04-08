package com.tableorder.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KakaoLoginRequest(
        @NotNull Long storeId,
        @NotNull Integer tableNumber,
        @NotBlank String kakaoAccessToken
) {}
