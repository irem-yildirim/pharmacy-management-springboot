package com.pharmacy.dto.response;

import com.pharmacy.entity.Sale;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private LocalDateTime saleDate;
    private Boolean isPrescriptionLogged;
    private List<SaleItemResponse> items;

    public static SaleResponse fromEntity(Sale sale) {
        if (sale == null) {
            return null;
        }
        return SaleResponse.builder()
                .id(sale.getId())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getId() : null)
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getName() : null)
                .totalAmount(sale.getTotalAmount())
                .saleDate(sale.getSaleDate())
                .isPrescriptionLogged(sale.getIsPrescriptionLogged())
                .items(sale.getItems() != null ? sale.getItems().stream()
                        .map(SaleItemResponse::fromEntity)
                        .collect(Collectors.toList()) : List.of())
                .build();
    }
}
