package com.pos.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SalesReportResponse {
    private String period;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private List<TopProductEntry> topProducts;

    @Data
    @Builder
    public static class TopProductEntry {
        private Long productId;
        private String productName;
        private long unitsSold;
    }
}
