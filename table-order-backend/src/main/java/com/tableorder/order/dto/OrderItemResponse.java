package com.tableorder.order.dto;

public record OrderItemResponse(Long id, String menuName, int quantity, int unitPrice,
                                 Long customerProfileId, String customerNickname) {}
