package com.tableorder.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Long id, String orderNumber, int totalAmount, String status,
                             List<OrderItemResponse> items, LocalDateTime createdAt) {}
