package com.pharmacy.controller;

import com.pharmacy.entity.Drug;
import com.pharmacy.entity.Purchase;
import com.pharmacy.repository.DrugRepository;
import com.pharmacy.repository.PurchaseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchases", description = "Stock batch procurement and inventory intake")
public class PurchaseController {

    private final PurchaseRepository purchaseRepository;
    private final DrugRepository drugRepository;

    @PostMapping
    @Operation(summary = "Create a purchase batch", description = "Records a new stock intake batch for an existing drug")
    public ResponseEntity<?> createPurchase(@Valid @RequestBody PurchaseBatchRequest request) {
        Drug drug = drugRepository.findById(request.getDrugBarcode()).orElse(null);
        if (drug == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Drug not found with barcode: " + request.getDrugBarcode());
        }

        Purchase purchase = Purchase.builder()
                .drug(drug)
                .originalQuantity(request.getQuantity())
                .remainingQuantity(request.getQuantity())
                .purchasePrice(request.getPurchasePrice())
                .expirationDate(request.getExpirationDate())
                .purchaseDate(LocalDate.now())
                .build();

        purchaseRepository.save(purchase);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Purchase batch created", "drugBarcode", request.getDrugBarcode()));
    }

    @Data
    public static class PurchaseBatchRequest {
        private String drugBarcode;
        private Integer quantity;
        private BigDecimal purchasePrice;
        private LocalDate expirationDate;
    }
}
