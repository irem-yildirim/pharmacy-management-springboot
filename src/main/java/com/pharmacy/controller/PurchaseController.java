package com.pharmacy.controller;

import com.pharmacy.entity.Drug;
import com.pharmacy.entity.Purchase;
import com.pharmacy.repository.DrugRepository;
import com.pharmacy.repository.PurchaseRepository;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseRepository purchaseRepository;
    private final DrugRepository drugRepository;

    @PostMapping
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

        Purchase saved = purchaseRepository.save(purchase);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Data
    public static class PurchaseBatchRequest {
        private String drugBarcode;
        private Integer quantity;
        private BigDecimal purchasePrice;
        private LocalDate expirationDate;
    }
}
