package com.pharmacy.controller;

import com.pharmacy.dto.request.DrugCreateRequest;
import com.pharmacy.dto.response.DrugResponse;
import com.pharmacy.entity.Brand;
import com.pharmacy.entity.Category;
import com.pharmacy.entity.Drug;
import com.pharmacy.entity.PresType;
import com.pharmacy.service.DrugService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drugs")
@RequiredArgsConstructor
public class DrugController {

    private final DrugService drugService;

    @GetMapping
    public ResponseEntity<List<DrugResponse>> getAllActive() {
        List<DrugResponse> responses = drugService.findAllActive().stream()
                .map(DrugResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{barcode}")
    public ResponseEntity<DrugResponse> getByBarcode(@PathVariable String barcode) {
        Drug drug = drugService.findByBarcode(barcode);
        return ResponseEntity.ok(DrugResponse.fromEntity(drug));
    }

    @PostMapping
    public ResponseEntity<DrugResponse> save(@Valid @RequestBody DrugCreateRequest request) {
        Drug drug = Drug.builder()
                .barcode(request.getBarcode())
                .name(request.getName())
                .category(Category.builder().id(request.getCategoryId()).build())
                .brand(Brand.builder().id(request.getBrandId()).build())
                .presType(request.getPresTypeId() != null ? PresType.builder().id(request.getPresTypeId()).build() : null)
                .currentSellingPrice(request.getCurrentSellingPrice())
                .minStockAlert(request.getMinStockAlert())
                .isActive(true)
                .build();
        Drug saved = drugService.save(drug);
        return ResponseEntity.status(HttpStatus.CREATED).body(DrugResponse.fromEntity(saved));
    }

    @DeleteMapping("/{barcode}")
    public ResponseEntity<Void> softDelete(@PathVariable String barcode) {
        drugService.softDelete(barcode);
        return ResponseEntity.noContent().build();
    }
}
