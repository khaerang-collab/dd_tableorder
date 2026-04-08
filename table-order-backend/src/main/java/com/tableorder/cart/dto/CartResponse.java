package com.tableorder.cart.dto;

import java.util.List;

public record CartResponse(Long sessionId, List<CartItemResponse> items, int totalAmount) {}
