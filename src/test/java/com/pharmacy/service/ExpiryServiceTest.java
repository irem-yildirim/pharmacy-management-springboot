package com.pharmacy.service;

import com.pharmacy.strategy.CriticalStrategy;
import com.pharmacy.strategy.ExpiredStrategy;
import com.pharmacy.strategy.OkStrategy;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpiryServiceTest {

    private final ExpiryService expiryService = new ExpiryService(
            new ExpiredStrategy(),
            new CriticalStrategy(),
            new OkStrategy()
    );

    @Test
    void testEvaluateExpired() {
        assertEquals("EXPIRED", expiryService.evaluateExpiry(LocalDate.now().minusDays(1)));
    }

    @Test
    void testEvaluateExpiredToday() {
        assertEquals("EXPIRED", expiryService.evaluateExpiry(LocalDate.now()));
    }

    @Test
    void testEvaluateCritical() {
        assertEquals("CRITICAL", expiryService.evaluateExpiry(LocalDate.now().plusDays(15)));
    }

    @Test
    void testEvaluateCriticalThreshold() {
        assertEquals("CRITICAL", expiryService.evaluateExpiry(LocalDate.now().plusDays(30)));
    }

    @Test
    void testEvaluateOk() {
        assertEquals("OK", expiryService.evaluateExpiry(LocalDate.now().plusDays(60)));
    }
}
