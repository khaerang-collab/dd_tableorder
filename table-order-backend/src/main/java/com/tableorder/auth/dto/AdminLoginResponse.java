package com.tableorder.auth.dto;

public record AdminLoginResponse(String token, Long storeId, String storeName, String adminUsername) {}
