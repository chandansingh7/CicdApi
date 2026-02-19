package com.pos.service;

import com.pos.dto.response.SalesReportResponse;
import com.pos.repository.OrderItemRepository;
import com.pos.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public SalesReportResponse getDailySummary(LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        return buildReport("Daily: " + date, from, to);
    }

    public SalesReportResponse getMonthlySummary(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = start.plusMonths(1).atStartOfDay();
        return buildReport("Monthly: " + year + "-" + String.format("%02d", month), from, to);
    }

    private SalesReportResponse buildReport(String period, LocalDateTime from, LocalDateTime to) {
        long totalOrders = orderRepository.countCompletedBetween(from, to);
        BigDecimal totalRevenue = orderRepository.sumTotalBetween(from, to);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        BigDecimal avgOrder = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Object[]> rawTop = orderItemRepository.findTopProductsBetween(from, to, PageRequest.of(0, 5));
        List<SalesReportResponse.TopProductEntry> topProducts = rawTop.stream()
                .map(row -> SalesReportResponse.TopProductEntry.builder()
                        .productId((Long) row[0])
                        .productName((String) row[1])
                        .unitsSold(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());

        return SalesReportResponse.builder()
                .period(period)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .averageOrderValue(avgOrder)
                .topProducts(topProducts)
                .build();
    }
}
