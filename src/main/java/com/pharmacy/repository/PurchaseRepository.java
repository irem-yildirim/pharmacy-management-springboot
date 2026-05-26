package com.pharmacy.repository;

import com.pharmacy.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc(
            String barcode, int minQty);

    @Query("SELECT COALESCE(SUM(p.remainingQuantity), 0) FROM Purchase p WHERE p.drug.barcode = :barcode AND p.remainingQuantity > 0")
    int sumRemainingByDrugBarcode(@Param("barcode") String barcode);

    List<Purchase> findByExpirationDateBeforeAndRemainingQuantityGreaterThan(LocalDate date, int minQty);

    @Query("SELECT COALESCE(SUM(p.remainingQuantity * p.purchasePrice), 0) FROM Purchase p WHERE p.expirationDate < CURRENT_DATE AND p.remainingQuantity > 0")
    BigDecimal calculateExpiredLoss();

    List<Purchase> findByDrug_BarcodeAndExpirationDateBeforeAndRemainingQuantityGreaterThan(
            String barcode, LocalDate date, int minQty);
}