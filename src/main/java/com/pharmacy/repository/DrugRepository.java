package com.pharmacy.repository;

import com.pharmacy.model.Drug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DrugRepository extends JpaRepository<Drug, String> {

    List<Drug> findByIsActiveTrue();
    boolean existsByBrand_Id(Long brandId);
    boolean existsByCategory_Id(Long categoryId);
}
