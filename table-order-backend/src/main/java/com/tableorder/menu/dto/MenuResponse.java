package com.tableorder.menu.dto;

public record MenuResponse(Long id, Long categoryId, String name, int price, String description,
                            String imageUrl, String badgeType, int displayOrder, boolean isAvailable) {}
