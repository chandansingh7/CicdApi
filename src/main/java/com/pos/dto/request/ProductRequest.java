package com.pos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    private String sku;
    private String barcode;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private Long categoryId;
    private String imageUrl;

    private boolean active = true;
    private int initialStock = 0;
    private int lowStockThreshold = 10;
}
