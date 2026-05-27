package com.pharmacy.service;

import com.pharmacy.dto.request.DrugCreateRequest;
import com.pharmacy.model.Brand;
import com.pharmacy.model.Category;
import com.pharmacy.model.Drug;
import com.pharmacy.model.PresType;
import com.pharmacy.model.Purchase;
import com.pharmacy.advice.DrugNotFoundException;
import com.pharmacy.repository.DrugRepository;
import com.pharmacy.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DrugService {

    private final DrugRepository drugRepository;
    private final PurchaseRepository purchaseRepository;

    public List<Drug> findAllActive() {
        return drugRepository.findByIsActiveTrue();
    }

    public Drug findByBarcode(String barcode) {
        return drugRepository.findById(barcode)
                .orElseThrow(() -> new DrugNotFoundException("Drug not found with barcode: " + barcode));
    }

    @Transactional
    public Drug create(DrugCreateRequest request) {
        Drug drug = Drug.builder()
                .barcode(request.getBarcode())
                .name(request.getName())
                .category(Category.builder().id(request.getCategoryId()).build())
                .brand(Brand.builder().id(request.getBrandId()).build())
                .presType(request.getPresTypeId() != null ? PresType.builder().id(request.getPresTypeId()).build() : null)
                .currentSellingPrice(request.getCurrentSellingPrice())
                .minStockAlert(request.getMinStockAlert())
                .isActive(true)
                .build();
        return drugRepository.save(drug);
    }

    @Transactional
    public Drug update(String barcode, Map<String, Object> payload) {
        Drug drug = findByBarcode(barcode);
        if (payload.containsKey("currentSellingPrice")) {
            drug.setCurrentSellingPrice(new BigDecimal(payload.get("currentSellingPrice").toString()));
        }
        if (payload.containsKey("minStockAlert")) {
            drug.setMinStockAlert(Integer.parseInt(payload.get("minStockAlert").toString()));
        }
        return drugRepository.save(drug);
    }

    @Transactional
    public void softDelete(String barcode) {
        Drug drug = findByBarcode(barcode);
        drug.setIsActive(false);
        drugRepository.save(drug);
    }

    public int getTotalStock(String barcode) {
        return purchaseRepository.sumRemainingByDrugBarcode(barcode);
    }

    public List<Purchase> getActiveBatches(String barcode) {
        return purchaseRepository.findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc(barcode, 0);
    }
}
