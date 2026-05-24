package com.pharmacy.controller;

import com.pharmacy.dto.response.DashboardStatsResponse;
import com.pharmacy.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Analytics and KPI endpoints for the admin dashboard")
public class DashboardController {

    private final FinanceService financeService;

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", description = "Returns today's revenue and profit calculated via FIFO costing")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        LocalDate today = LocalDate.now();
        BigDecimal dailyRevenue = financeService.calculateDailyRevenue(today);
        BigDecimal dailyProfit = financeService.calculateDailyProfit(today);

        DashboardStatsResponse response = DashboardStatsResponse.builder()
                .dailyRevenue(dailyRevenue)
                .dailyProfit(dailyProfit)
                .build();
        return ResponseEntity.ok(response);
    }
}
