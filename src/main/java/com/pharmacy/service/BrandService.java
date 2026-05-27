package com.pharmacy.service;

import com.pharmacy.advice.DuplicateEntryException;
import com.pharmacy.dto.request.BrandCreateRequest;
import com.pharmacy.model.Brand;
import com.pharmacy.repository.BrandRepository;
import com.pharmacy.repository.DrugRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class BrandService {

    private final BrandRepository brandRepository;
    private final DrugRepository drugRepository;

    public List<Brand> findAllActive() {
        return brandRepository.findByIsActiveTrue();
    }

    @Transactional
    public Brand create(BrandCreateRequest request) {
        Brand brand = Brand.builder()
                .name(request.getName())
                .isActive(true)
                .build();
        return brandRepository.save(brand);
    }

    @Transactional
    public void softDelete(Long id) {
        if (drugRepository.existsByBrand_Id(id)) {
            throw new DuplicateEntryException("Cannot delete: This brand has linked drugs. Remove or reassign them first.");
        }
        brandRepository.findById(id).ifPresent(brand -> {
            brand.setIsActive(false);
            brandRepository.save(brand);
        });
    }
}
