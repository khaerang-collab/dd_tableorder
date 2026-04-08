package com.tableorder.staffcall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StaffCallRequest(
    @NotBlank @Size(max = 50) String reason,
    @Size(max = 200) String message
) {}
