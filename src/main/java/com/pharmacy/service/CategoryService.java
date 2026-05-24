package com.pharmacy.service;

import com.pharmacy.advice.DuplicateEntryException;
import com.pharmacy.model.Category;
import com.pharmacy.repository.CategoryRepository;
import com.pharmacy.repository.DrugRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final DrugRepository drugRepository;

    public List<Category> findAllActive() {
        return categoryRepository.findByIsActiveTrue();
    }

    @Transactional
    public Category save(Category category) {
        category.setIsActive(true);
        return categoryRepository.save(category);
    }

    @Transactional
    public void softDelete(Long id) {
        if (drugRepository.existsByCategory_Id(id)) {
            throw new DuplicateEntryException("Cannot delete: This category has linked drugs. Remove or reassign them first.");
        }
        categoryRepository.findById(id).ifPresent(category -> {
            category.setIsActive(false);
            categoryRepository.save(category);
        });
    }
}
