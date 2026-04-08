package com.tableorder.order.dto;

import java.util.List;

public record TableOrderSummary(Long tableId, Integer tableNumber, Long sessionId,
                                 int totalAmount, int orderCount, List<OrderResponse> recentOrders) {}
