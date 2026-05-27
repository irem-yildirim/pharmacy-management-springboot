package com.pharmacy.controller.api;

import com.pharmacy.dto.request.CategoryCreateRequest;
import com.pharmacy.dto.response.CategoryResponse;
import com.pharmacy.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Drug classification category management")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "List active categories", description = "Returns all non-deleted drug categories")
    public ResponseEntity<List<CategoryResponse>> getAllActive() {
        List<CategoryResponse> responses = categoryService.findAllActive().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Operation(summary = "Create a new category", description = "Adds a new drug classification category")
    public ResponseEntity<CategoryResponse> save(@Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryResponse.fromEntity(categoryService.create(request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a category", description = "Blocks deletion if linked drugs exist; otherwise sets isActive=false")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
