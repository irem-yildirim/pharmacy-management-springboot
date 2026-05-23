package com.pharmacy.repository;

import com.pharmacy.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    @Query("SELECT COALESCE(SUM(si.unitPrice * si.quantity), 0) FROM SaleItem si WHERE si.sale.saleDate BETWEEN :start AND :end")
    BigDecimal calculateDailyRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(si.purchase.purchasePrice * si.quantity), 0) FROM SaleItem si WHERE si.sale.saleDate BETWEEN :start AND :end")
    BigDecimal calculateDailyCost(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
