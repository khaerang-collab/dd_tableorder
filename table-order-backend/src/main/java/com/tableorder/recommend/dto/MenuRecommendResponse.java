package com.tableorder.recommend.dto;

public record MenuRecommendResponse(Long menuId, String menuName, int price, String imageUrl, int pairCount) {}
