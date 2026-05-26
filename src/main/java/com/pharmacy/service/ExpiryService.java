package com.pharmacy.service;

import com.pharmacy.model.Purchase;
import com.pharmacy.repository.PurchaseRepository;
import com.pharmacy.strategy.CriticalStrategy;
import com.pharmacy.strategy.ExpiredStrategy;
import com.pharmacy.strategy.ExpiryStrategy;
import com.pharmacy.strategy.OkStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpiryService {

    private final ExpiredStrategy expiredStrategy;
    private final CriticalStrategy criticalStrategy;
    private final OkStrategy okStrategy;
    private final PurchaseRepository purchaseRepository;

    public String evaluateExpiry(LocalDate expirationDate) {
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
        ExpiryStrategy strategy = resolveStrategy(daysRemaining);
        return strategy.evaluate(daysRemaining);
    }

    private ExpiryStrategy resolveStrategy(long daysRemaining) {
        if (daysRemaining <= 0) return expiredStrategy;
        if (daysRemaining <= 30) return criticalStrategy;
        return okStrategy;
    }

    @Transactional
    public BigDecimal disposeExpiredBatches(String barcode) {
        List<Purchase> expired = purchaseRepository
                .findByDrug_BarcodeAndExpirationDateBeforeAndRemainingQuantityGreaterThan(
                        barcode, LocalDate.now(), 0);
        BigDecimal totalLoss = BigDecimal.ZERO;
        for (Purchase batch : expired) {
            totalLoss = totalLoss.add(
                    batch.getPurchasePrice().multiply(BigDecimal.valueOf(batch.getRemainingQuantity())));
            batch.setRemainingQuantity(0);
            purchaseRepository.save(batch);
        }
        return totalLoss;
    }
}
