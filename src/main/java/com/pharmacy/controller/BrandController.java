package com.pharmacy.controller;

import com.pharmacy.entity.Brand;
import com.pharmacy.repository.BrandRepository;
import com.pharmacy.repository.DrugRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brands", description = "Pharmaceutical manufacturer brand management")
public class BrandController {

    private final BrandRepository brandRepository;
    private final DrugRepository drugRepository;

    @GetMapping
    @Operation(summary = "List active brands", description = "Returns all non-deleted drug brands/manufacturers")
    public ResponseEntity<List<Brand>> getAllActive() {
        return ResponseEntity.ok(brandRepository.findByIsActiveTrue());
    }

    @PostMapping
    @Operation(summary = "Create a new brand", description = "Adds a new pharmaceutical manufacturer brand")
    public ResponseEntity<Brand> save(@Valid @RequestBody Brand brand) {
        brand.setIsActive(true);
        Brand saved = brandRepository.save(brand);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a brand", description = "Blocks deletion if linked drugs exist; otherwise sets isActive=false")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (drugRepository.existsByBrand_Id(id)) {
            return ResponseEntity.badRequest().body("Cannot delete: This brand has linked drugs. Remove or reassign them first.");
        }
        brandRepository.findById(id).ifPresent(brand -> {
            brand.setIsActive(false);
            brandRepository.save(brand);
        });
        return ResponseEntity.noContent().build();
    }
}
