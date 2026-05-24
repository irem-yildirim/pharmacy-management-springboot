package com.pharmacy.service;

import com.pharmacy.advice.DrugNotFoundException;
import com.pharmacy.model.Purchase;
import com.pharmacy.repository.DrugRepository;
import com.pharmacy.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final DrugRepository drugRepository;

    @Transactional
    public Purchase createPurchase(String drugBarcode, Integer quantity, BigDecimal purchasePrice, LocalDate expirationDate) {
        drugRepository.findById(drugBarcode)
                .orElseThrow(() -> new DrugNotFoundException("Drug not found with barcode: " + drugBarcode));

        Purchase purchase = Purchase.builder()
                .drug(drugRepository.findById(drugBarcode).get())
                .originalQuantity(quantity)
                .remainingQuantity(quantity)
                .purchasePrice(purchasePrice)
                .expirationDate(expirationDate)
                .purchaseDate(LocalDate.now())
                .build();

        return purchaseRepository.save(purchase);
    }

    public int getTotalStock(String barcode) {
        return purchaseRepository.sumRemainingByDrugBarcode(barcode);
    }
}
