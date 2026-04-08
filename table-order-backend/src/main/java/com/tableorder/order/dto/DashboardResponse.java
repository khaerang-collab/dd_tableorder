package com.tableorder.order.dto;

import java.util.List;

public record DashboardResponse(List<TableOrderSummary> tables) {}
