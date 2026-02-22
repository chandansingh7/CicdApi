package com.pos.dto.response;

public record InventoryStats(long total, long inStock, long lowStock, long outOfStock) {}
