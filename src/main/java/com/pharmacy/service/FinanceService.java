package com.pharmacy.service;

import com.pharmacy.repository.SaleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final SaleItemRepository saleItemRepository;

    public BigDecimal calculateDailyRevenue(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return saleItemRepository.calculateDailyRevenue(start, end);
    }

    public BigDecimal calculateDailyProfit(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        BigDecimal revenue = saleItemRepository.calculateDailyRevenue(start, end);
        BigDecimal cost = saleItemRepository.calculateDailyCost(start, end);
        return revenue.subtract(cost);
    }
}
