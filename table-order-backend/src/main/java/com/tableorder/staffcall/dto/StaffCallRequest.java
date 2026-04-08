package com.tableorder.staffcall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StaffCallRequest(
        @NotNull Long tableId,
        @NotNull Integer tableNumber,
        @NotBlank String reason,
        String customMessage
) {}
