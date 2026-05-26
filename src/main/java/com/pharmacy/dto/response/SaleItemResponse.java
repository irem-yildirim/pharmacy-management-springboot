package com.pharmacy.dto.response;

import com.pharmacy.model.SaleItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleItemResponse {

    private Long id;
    private String drugBarcode;
    private String drugName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal purchasePrice;
    private LocalDate expirationDate;
    private Long purchaseId;

    public static SaleItemResponse fromEntity(SaleItem item) {
        if (item == null) {
            return null;
        }
        return SaleItemResponse.builder()
                .id(item.getId())
                .drugBarcode(item.getPurchase() != null && item.getPurchase().getDrug() != null ? item.getPurchase().getDrug().getBarcode() : null)
                .drugName(item.getPurchase() != null && item.getPurchase().getDrug() != null ? item.getPurchase().getDrug().getName() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .purchasePrice(item.getPurchase() != null ? item.getPurchase().getPurchasePrice() : null)
                .expirationDate(item.getPurchase() != null ? item.getPurchase().getExpirationDate() : null)
                .purchaseId(item.getPurchase() != null ? item.getPurchase().getId() : null)
                .build();
    }
}
