package com.pharmacy.service;

import com.pharmacy.model.Purchase;
import com.pharmacy.model.Sale;
import com.pharmacy.repository.PurchaseRepository;
import com.pharmacy.repository.SaleItemRepository;
import com.pharmacy.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final SaleItemRepository saleItemRepository;
    private final PurchaseRepository purchaseRepository;
    private final SaleRepository saleRepository;

    public BigDecimal calculateDailyRevenue(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return saleItemRepository.calculateDailyRevenue(start, end);
    }

    public BigDecimal calculateExpiredLoss() {
        BigDecimal loss = purchaseRepository.calculateExpiredLoss();
        return loss != null ? loss : BigDecimal.ZERO;
    }

    public BigDecimal calculateDailyProfit(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        BigDecimal revenue = saleItemRepository.calculateDailyRevenue(start, end);
        BigDecimal cost = saleItemRepository.calculateDailyCost(start, end);
        BigDecimal loss = calculateExpiredLoss();
        return revenue.subtract(cost).subtract(loss);
    }

    public BigDecimal calculateTotalRevenue() {
        BigDecimal revenue = saleItemRepository.calculateTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public BigDecimal calculateTotalCost() {
        BigDecimal cost = saleItemRepository.calculateTotalCost();
        return cost != null ? cost : BigDecimal.ZERO;
    }

    public BigDecimal calculateTotalProfit() {
        BigDecimal revenue = calculateTotalRevenue();
        BigDecimal cost = calculateTotalCost();
        BigDecimal loss = calculateExpiredLoss();
        return revenue.subtract(cost).subtract(loss);
    }

    public List<Map<String, Object>> getTransactionLedger() {
        List<Map<String, Object>> ledger = new ArrayList<>();

        List<Sale> sales = saleRepository.findAllByOrderBySaleDateDesc();
        for (Sale sale : sales) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("type", "SALE");
            entry.put("date", sale.getSaleDate());
            entry.put("description", "Sale #" + sale.getId() + (sale.getCustomer() != null ? " - " + sale.getCustomer().getName() : " - Walk-in"));
            entry.put("amount", sale.getTotalAmount());
            entry.put("colorClass", "text-emerald-600");
            entry.put("bgClass", "bg-emerald-50");
            ledger.add(entry);
        }

        List<Purchase> purchases = purchaseRepository.findAllByOrderByPurchaseDateDesc();
        for (Purchase purchase : purchases) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("type", "PURCHASE");
            entry.put("date", purchase.getPurchaseDate().atStartOfDay());
            entry.put("description", "Stock Intake: " + (purchase.getDrug() != null ? purchase.getDrug().getName() : "Unknown") + " x" + purchase.getOriginalQuantity() + " @$" + purchase.getPurchasePrice());
            entry.put("amount", purchase.getPurchasePrice().multiply(BigDecimal.valueOf(purchase.getOriginalQuantity())).negate());
            entry.put("colorClass", "text-rose-600");
            entry.put("bgClass", "bg-rose-50");
            ledger.add(entry);
        }

        List<Purchase> disposedCandidates = purchaseRepository.findByRemainingQuantityAndExpirationDateBefore(0, LocalDate.now());
        for (Purchase batch : disposedCandidates) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("type", "DISPOSAL");
            entry.put("date", batch.getExpirationDate().atStartOfDay());
            entry.put("description", "Expired Batch #" + batch.getId() + ": " + (batch.getDrug() != null ? batch.getDrug().getName() : "Unknown") + " (Qty lost: " + batch.getOriginalQuantity() + ")");
            BigDecimal loss = batch.getPurchasePrice().multiply(BigDecimal.valueOf(batch.getOriginalQuantity()));
            entry.put("amount", loss.negate());
            entry.put("colorClass", "text-red-700");
            entry.put("bgClass", "bg-red-50");
            ledger.add(entry);
        }

        ledger.sort(Comparator.comparing(e -> ((java.time.LocalDateTime) e.get("date")), Comparator.reverseOrder()));

        return ledger;
    }
}
