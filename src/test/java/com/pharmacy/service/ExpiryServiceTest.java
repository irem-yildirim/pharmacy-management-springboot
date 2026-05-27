package com.pharmacy.service;

import com.pharmacy.repository.PurchaseRepository;
import com.pharmacy.strategy.CriticalStrategy;
import com.pharmacy.strategy.ExpiredStrategy;
import com.pharmacy.strategy.ExpiryStrategy;
import com.pharmacy.strategy.OkStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ExpiryServiceTest {

    private ExpiryService expiryService;

    @BeforeEach
    void setUp() throws Exception {
        expiryService = new ExpiryService();

        List<ExpiryStrategy> strategies = List.of(
                new ExpiredStrategy(),
                new CriticalStrategy(),
                new OkStrategy()
        );

        Field strategiesField = ExpiryService.class.getDeclaredField("strategies");
        strategiesField.setAccessible(true);
        strategiesField.set(expiryService, strategies);

        Field repoField = ExpiryService.class.getDeclaredField("purchaseRepository");
        repoField.setAccessible(true);
        repoField.set(expiryService, mock(PurchaseRepository.class));
    }

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
