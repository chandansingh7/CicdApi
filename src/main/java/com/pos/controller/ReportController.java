package com.pos.controller;

import com.pos.dto.response.ApiResponse;
import com.pos.dto.response.SalesReportResponse;
import com.pos.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales/daily")
    public ResponseEntity<ApiResponse<SalesReportResponse>> dailySales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(reportService.getDailySummary(target)));
    }

    @GetMapping("/sales/monthly")
    public ResponseEntity<ApiResponse<SalesReportResponse>> monthlySales(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int month) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getMonthlySummary(year, month)));
    }
}
