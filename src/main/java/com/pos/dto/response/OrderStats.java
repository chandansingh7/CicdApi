package com.pos.dto.response;

import java.math.BigDecimal;

public record OrderStats(
        long total,
        long completed,
        long pending,
        long cancelled,
        long refunded,
        BigDecimal totalRevenue) {}
