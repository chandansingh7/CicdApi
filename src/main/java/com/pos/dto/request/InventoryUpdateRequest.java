package com.pos.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class InventoryUpdateRequest {
    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private int lowStockThreshold = 10;
}
