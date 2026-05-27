package com.pharmacy.service;

import com.pharmacy.model.Purchase;
import com.pharmacy.repository.PurchaseRepository;
import com.pharmacy.strategy.ExpiryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ExpiryService {

    @Autowired
    private List<ExpiryStrategy> strategies;

    @Autowired
    private PurchaseRepository purchaseRepository;

    public String evaluateExpiry(LocalDate expirationDate) {
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
        return strategies.stream()
                .filter(s -> s.isApplicable(daysRemaining))
                .findFirst()
                .map(s -> s.evaluate(daysRemaining))
                .orElse("OK");
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
