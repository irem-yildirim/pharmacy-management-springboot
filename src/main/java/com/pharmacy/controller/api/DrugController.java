package com.pharmacy.controller.api;

import com.pharmacy.dto.request.DrugCreateRequest;
import com.pharmacy.dto.response.DrugResponse;
import com.pharmacy.dto.response.PurchaseBatchResponse;
import com.pharmacy.model.Drug;
import com.pharmacy.service.DrugService;
import com.pharmacy.service.ExpiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drugs")
@RequiredArgsConstructor
@Tag(name = "Drugs", description = "Pharmaceutical product catalog management")
public class DrugController {

    private final DrugService drugService;
    private final ExpiryService expiryService;

    @GetMapping
    @Operation(summary = "List all active drugs", description = "Returns all active drugs with total stock and active purchase batches")
    public ResponseEntity<List<DrugResponse>> getAllActive() {
        List<DrugResponse> responses = drugService.findAllActive().stream()
                .map(drug -> {
                    DrugResponse dto = DrugResponse.fromEntity(drug);
                    int totalStock = drugService.getTotalStock(drug.getBarcode());
                    dto.setTotalStock(totalStock);
                    dto.setBatches(drugService.getActiveBatches(drug.getBarcode()).stream()
                            .map(PurchaseBatchResponse::fromEntity)
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{barcode}")
    @Operation(summary = "Get drug by barcode", description = "Returns a single drug with stock and batch details")
    public ResponseEntity<DrugResponse> getByBarcode(@PathVariable String barcode) {
        Drug drug = drugService.findByBarcode(barcode);
        DrugResponse dto = DrugResponse.fromEntity(drug);
        int totalStock = drugService.getTotalStock(barcode);
        dto.setTotalStock(totalStock);
        dto.setBatches(drugService.getActiveBatches(barcode).stream()
                .map(PurchaseBatchResponse::fromEntity)
                .collect(Collectors.toList()));
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @Operation(summary = "Register a new drug", description = "Creates a new pharmaceutical product record in the catalog")
    public ResponseEntity<DrugResponse> save(@Valid @RequestBody DrugCreateRequest request) {
        Drug saved = drugService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DrugResponse.fromEntity(saved));
    }

    @DeleteMapping("/{barcode}")
    @Operation(summary = "Soft-delete a drug", description = "Sets isActive=false; does not physically remove the record")
    public ResponseEntity<Void> softDelete(@PathVariable String barcode) {
        drugService.softDelete(barcode);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{barcode}/dispose")
    @Operation(summary = "Dispose expired batches for a drug", description = "Zeroes out remaining quantities for all expired batches and returns the financial loss")
    public ResponseEntity<Map<String, Object>> disposeExpired(@PathVariable String barcode) {
        BigDecimal loss = expiryService.disposeExpiredBatches(barcode);
        return ResponseEntity.ok(Map.of(
                "message", "Expired batches disposed",
                "drugBarcode", barcode,
                "loss", loss));
    }

    @PutMapping("/{barcode}")
    @Operation(summary = "Update drug price or stock alert", description = "Modifies the current selling price and minimum stock alert threshold")
    public ResponseEntity<DrugResponse> update(
            @PathVariable String barcode,
            @RequestBody Map<String, Object> payload) {
        Drug updated = drugService.update(barcode, payload);
        return ResponseEntity.ok(DrugResponse.fromEntity(updated));
    }
}
