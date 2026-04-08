package com.tableorder.cart.dto;

import java.time.LocalDateTime;

public record CartItemResponse(Long id, Long menuId, String menuName, int menuPrice, String imageUrl,
                                Long customerProfileId, String nickname, String profileImageUrl,
                                String deviceId, int quantity, LocalDateTime createdAt) {}
