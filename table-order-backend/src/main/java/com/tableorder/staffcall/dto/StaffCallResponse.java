package com.tableorder.staffcall.dto;

import java.time.LocalDateTime;

public record StaffCallResponse(
    Long id,
    Long tableId,
    int tableNumber,
    String reason,
    String message,
    String status,
    LocalDateTime calledAt,
    LocalDateTime attendedAt
) {}
