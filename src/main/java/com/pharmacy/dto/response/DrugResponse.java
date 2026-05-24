package com.pharmacy.dto.response;

import com.pharmacy.entity.Drug;
import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugResponse {

    private String barcode;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Long presTypeId;
    private String presTypeName;
    private BigDecimal currentSellingPrice;
    private Integer minStockAlert;
    private Boolean isActive;
    private Integer totalStock;
    private List<PurchaseBatchResponse> batches;

    public static DrugResponse fromEntity(Drug drug) {
        if (drug == null) {
            return null;
        }
        return DrugResponse.builder()
                .barcode(drug.getBarcode())
                .name(drug.getName())
                .categoryId(drug.getCategory() != null ? drug.getCategory().getId() : null)
                .categoryName(drug.getCategory() != null ? drug.getCategory().getName() : null)
                .brandId(drug.getBrand() != null ? drug.getBrand().getId() : null)
                .brandName(drug.getBrand() != null ? drug.getBrand().getName() : null)
                .presTypeId(drug.getPresType() != null ? drug.getPresType().getId() : null)
                .presTypeName(drug.getPresType() != null ? drug.getPresType().getName() : null)
                .currentSellingPrice(drug.getCurrentSellingPrice())
                .minStockAlert(drug.getMinStockAlert())
                .isActive(drug.getIsActive())
                .build();
    }
}
