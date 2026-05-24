package com.pharmacy.dto.response;

import com.pharmacy.model.Purchase;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseBatchResponse {

    private Long id;
    private BigDecimal purchasePrice;
    private LocalDate expirationDate;
    private Integer remainingQuantity;
    private String status;

    public static PurchaseBatchResponse fromEntity(Purchase purchase) {
        if (purchase == null) {
            return null;
        }

        String status;
        LocalDate now = LocalDate.now();
        if (purchase.getExpirationDate().isBefore(now)) {
            status = "EXPIRED";
        } else if (purchase.getExpirationDate().isBefore(now.plusDays(30))) {
            status = "CRITICAL";
        } else if (purchase.getExpirationDate().isBefore(now.plusDays(90))) {
            status = "WARNING";
        } else {
            status = "OK";
        }

        return PurchaseBatchResponse.builder()
                .id(purchase.getId())
                .purchasePrice(purchase.getPurchasePrice())
                .expirationDate(purchase.getExpirationDate())
                .remainingQuantity(purchase.getRemainingQuantity())
                .status(status)
                .build();
    }
}
