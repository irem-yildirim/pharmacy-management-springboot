package com.pharmacy.controller;

import com.pharmacy.entity.Brand;
import com.pharmacy.repository.BrandRepository;
import com.pharmacy.repository.DrugRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandRepository brandRepository;
    private final DrugRepository drugRepository;

    @GetMapping
    public ResponseEntity<List<Brand>> getAllActive() {
        return ResponseEntity.ok(brandRepository.findByIsActiveTrue());
    }

    @PostMapping
    public ResponseEntity<Brand> save(@Valid @RequestBody Brand brand) {
        brand.setIsActive(true);
        Brand saved = brandRepository.save(brand);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (drugRepository.existsByBrand_Id(id)) {
            return ResponseEntity.badRequest().body("Linked drugs exist!");
        }
        brandRepository.findById(id).ifPresent(brand -> {
            brand.setIsActive(false);
            brandRepository.save(brand);
        });
        return ResponseEntity.noContent().build();
    }
}
