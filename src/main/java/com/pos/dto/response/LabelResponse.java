package com.pos.dto.response;

import com.pos.entity.Label;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LabelResponse {

    private Long id;
    private String barcode;
    private String name;
    private BigDecimal price;
    private String sku;
    private Long categoryId;
    private String categoryName;
    private Long productId;
    private LocalDateTime createdAt;

    public static LabelResponse from(Label l) {
        return LabelResponse.builder()
                .id(l.getId())
                .barcode(l.getBarcode())
                .name(l.getName())
                .price(l.getPrice())
                .sku(l.getSku())
                .categoryId(l.getCategory() != null ? l.getCategory().getId() : null)
                .categoryName(l.getCategory() != null ? l.getCategory().getName() : null)
                .productId(l.getProduct() != null ? l.getProduct().getId() : null)
                .createdAt(l.getCreatedAt())
                .build();
    }
}
