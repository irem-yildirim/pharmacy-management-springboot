package com.pharmacy.controller;

import com.pharmacy.entity.Category;
import com.pharmacy.repository.CategoryRepository;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Drug classification category management")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final DrugRepository drugRepository;

    @GetMapping
    @Operation(summary = "List active categories", description = "Returns all non-deleted drug categories")
    public ResponseEntity<List<Category>> getAllActive() {
        return ResponseEntity.ok(categoryRepository.findByIsActiveTrue());
    }

    @PostMapping
    @Operation(summary = "Create a new category", description = "Adds a new drug classification category")
    public ResponseEntity<Category> save(@Valid @RequestBody Category category) {
        category.setIsActive(true);
        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a category", description = "Blocks deletion if linked drugs exist; otherwise sets isActive=false")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (drugRepository.existsByCategory_Id(id)) {
            return ResponseEntity.badRequest().body("Cannot delete: This category has linked drugs. Remove or reassign them first.");
        }
        categoryRepository.findById(id).ifPresent(category -> {
            category.setIsActive(false);
            categoryRepository.save(category);
        });
        return ResponseEntity.noContent().build();
    }
}
