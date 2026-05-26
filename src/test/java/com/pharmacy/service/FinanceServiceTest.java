package com.pharmacy.service;

import com.pharmacy.repository.PurchaseRepository;
import com.pharmacy.repository.SaleItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private SaleItemRepository saleItemRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private FinanceService financeService;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        lenient().when(purchaseRepository.calculateExpiredLoss()).thenReturn(BigDecimal.ZERO);
    }

    @Test
    void testCalculateDailyRevenue() {
        when(saleItemRepository.calculateDailyRevenue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("1500.00"));

        BigDecimal result = financeService.calculateDailyRevenue(today);

        assertEquals(new BigDecimal("1500.00"), result);
        verify(saleItemRepository, times(1)).calculateDailyRevenue(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testCalculateDailyRevenueZero() {
        when(saleItemRepository.calculateDailyRevenue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        BigDecimal result = financeService.calculateDailyRevenue(today);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testCalculateDailyProfit() {
        when(saleItemRepository.calculateDailyRevenue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("1500.00"));
        when(saleItemRepository.calculateDailyCost(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("1000.00"));

        BigDecimal result = financeService.calculateDailyProfit(today);

        assertEquals(new BigDecimal("500.00"), result);
    }

    @Test
    void testCalculateDailyProfitZero() {
        when(saleItemRepository.calculateDailyRevenue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(saleItemRepository.calculateDailyCost(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        BigDecimal result = financeService.calculateDailyProfit(today);

        assertEquals(BigDecimal.ZERO, result);
    }
}
