package com.pharmacy.service;

import com.pharmacy.strategy.CriticalStrategy;
import com.pharmacy.strategy.ExpiredStrategy;
import com.pharmacy.strategy.ExpiryStrategy;
import com.pharmacy.strategy.OkStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ExpiryService {

    private final ExpiredStrategy expiredStrategy;
    private final CriticalStrategy criticalStrategy;
    private final OkStrategy okStrategy;

    public String evaluateExpiry(LocalDate expirationDate) {
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
        ExpiryStrategy strategy = resolveStrategy(daysRemaining);
        return strategy.evaluate(daysRemaining);
    }

    // Select the appropriate strategy based on days remaining
    private ExpiryStrategy resolveStrategy(long daysRemaining) {
        if (daysRemaining <= 0) return expiredStrategy;
        if (daysRemaining <= 30) return criticalStrategy;
        return okStrategy;
    }
}
