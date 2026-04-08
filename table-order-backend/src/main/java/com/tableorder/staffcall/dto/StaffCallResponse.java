package com.tableorder.staffcall.dto;

import java.time.LocalDateTime;

public record StaffCallResponse(Long id, Long tableId, int tableNumber, String reason,
                                 String customMessage, String status, LocalDateTime createdAt) {}
