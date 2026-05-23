package com.pharmacy.service;

import com.pharmacy.dto.request.SaleItemRequest;
import com.pharmacy.entity.Drug;
import com.pharmacy.entity.Purchase;
import com.pharmacy.entity.Sale;
import com.pharmacy.entity.SaleItem;
import com.pharmacy.exception.DrugNotFoundException;
import com.pharmacy.exception.InsufficientStockException;
import com.pharmacy.exception.PrescriptionRequiredException;
import com.pharmacy.repository.DrugRepository;
import com.pharmacy.repository.PurchaseRepository;
import com.pharmacy.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final DrugRepository drugRepository;
    private final PurchaseRepository purchaseRepository;

    @Transactional
    public Sale createSale(List<SaleItemRequest> requests, boolean prescriptionLogged) {
        Sale sale = Sale.builder()
                .saleDate(LocalDateTime.now())
                .isPrescriptionLogged(prescriptionLogged)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SaleItemRequest request : requests) {
            Drug drug = drugRepository.findById(request.getBarcode())
                    .filter(Drug::getIsActive)
                    .orElseThrow(() -> new DrugNotFoundException("Drug not found with barcode: " + request.getBarcode()));

            // Prescription check: riskLevel >= 1 requires prescription info
            if (drug.getPresType() != null
                    && drug.getPresType().getRiskLevel() != null
                    && drug.getPresType().getRiskLevel() >= 1
                    && !prescriptionLogged) {
                throw new PrescriptionRequiredException(
                        "Prescription required for drug: " + drug.getName());
            }

            List<SaleItem> deductedItems = deductFromBatches(drug, request.getQuantity(), sale);
            sale.getItems().addAll(deductedItems);

            for (SaleItem item : deductedItems) {
                totalAmount = totalAmount.add(
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        sale.setTotalAmount(totalAmount);
        return saleRepository.save(sale);
    }

    // FIFO: deduct requested quantity from oldest-expiry batches first
    private List<SaleItem> deductFromBatches(Drug drug, int requestedQty, Sale sale) {
        List<Purchase> batches = purchaseRepository
                .findByDrug_BarcodeAndRemainingQuantityGreaterThanOrderByExpirationDateAsc(
                        drug.getBarcode(), 0);

        int totalAvailable = batches.stream().mapToInt(Purchase::getRemainingQuantity).sum();
        if (totalAvailable < requestedQty) {
            throw new InsufficientStockException(
                    "Insufficient stock for " + drug.getName()
                            + ". Requested: " + requestedQty + ", Available: " + totalAvailable);
        }

        List<SaleItem> saleItems = new ArrayList<>();
        int remaining = requestedQty;

        for (Purchase batch : batches) {
            if (remaining <= 0) break;

            int deduct = Math.min(remaining, batch.getRemainingQuantity());
            batch.setRemainingQuantity(batch.getRemainingQuantity() - deduct);
            purchaseRepository.save(batch);

            SaleItem saleItem = SaleItem.builder()
                    .sale(sale)
                    .purchase(batch)
                    .quantity(deduct)
                    .unitPrice(drug.getCurrentSellingPrice())
                    .build();
            saleItems.add(saleItem);

            remaining -= deduct;
        }

        return saleItems;
    }
}
