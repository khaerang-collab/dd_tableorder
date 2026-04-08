package com.tableorder.table.dto;

public record TableResponse(Long id, Long storeId, Integer tableNumber, String qrCodeUrl) {}
