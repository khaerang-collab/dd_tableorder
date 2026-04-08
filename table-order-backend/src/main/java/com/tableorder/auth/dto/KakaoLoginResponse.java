package com.tableorder.auth.dto;

public record KakaoLoginResponse(
        String token, Long storeId, String storeName,
        Integer tableNumber, Long tableId, Long sessionId,
        Long profileId, String nickname, String profileImageUrl
) {}
