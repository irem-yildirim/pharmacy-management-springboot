package com.pharmacy.controller;

import com.pharmacy.entity.Category;
import com.pharmacy.repository.CategoryRepository;
import com.pharmacy.repository.DrugRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final DrugRepository drugRepository;

    @GetMapping
    public ResponseEntity<List<Category>> getAllActive() {
        return ResponseEntity.ok(categoryRepository.findByIsActiveTrue());
    }

    @PostMapping
    public ResponseEntity<Category> save(@Valid @RequestBody Category category) {
        category.setIsActive(true);
        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
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
