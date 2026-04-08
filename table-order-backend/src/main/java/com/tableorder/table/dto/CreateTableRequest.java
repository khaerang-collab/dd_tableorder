package com.tableorder.table.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateTableRequest(@NotNull @Min(1) Integer tableNumber, String baseUrl) {}
