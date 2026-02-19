package com.pos.dto.response;

import com.pos.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String sku;
    private String barcode;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
    private boolean active;
    private int quantity;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product p, int quantity) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .sku(p.getSku())
                .barcode(p.getBarcode())
                .price(p.getPrice())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .imageUrl(p.getImageUrl())
                .active(p.isActive())
                .quantity(quantity)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
