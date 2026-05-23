package com.pharmacy.service;

import com.pharmacy.entity.Drug;
import com.pharmacy.exception.DrugNotFoundException;
import com.pharmacy.repository.DrugRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugRepository drugRepository;

    public List<Drug> findAllActive() {
        return drugRepository.findByIsActiveTrue();
    }

    public Drug findByBarcode(String barcode) {
        return drugRepository.findById(barcode)
                .orElseThrow(() -> new DrugNotFoundException("Drug not found with barcode: " + barcode));
    }

    @Transactional
    public Drug save(Drug drug) {
        return drugRepository.save(drug);
    }

    @Transactional
    public void softDelete(String barcode) {
        Drug drug = findByBarcode(barcode);
        drug.setIsActive(false);
        drugRepository.save(drug);
    }
}
