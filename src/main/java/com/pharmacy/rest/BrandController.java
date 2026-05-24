package com.pharmacy.rest;

import com.pharmacy.dto.response.BrandResponse;
import com.pharmacy.model.Brand;
import com.pharmacy.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brands", description = "Pharmaceutical manufacturer brand management")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "List active brands", description = "Returns all non-deleted drug brands/manufacturers")
    public ResponseEntity<List<BrandResponse>> getAllActive() {
        List<BrandResponse> responses = brandService.findAllActive().stream()
                .map(BrandResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Operation(summary = "Create a new brand", description = "Adds a new pharmaceutical manufacturer brand")
    public ResponseEntity<BrandResponse> save(@Valid @RequestBody Brand brand) {
        Brand saved = brandService.save(brand);
        return ResponseEntity.status(HttpStatus.CREATED).body(BrandResponse.fromEntity(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a brand", description = "Blocks deletion if linked drugs exist; otherwise sets isActive=false")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        brandService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
