package com.pharmacy.repository;

import com.pharmacy.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByUser_IdOrderBySaleDateDesc(Long userId);
}
