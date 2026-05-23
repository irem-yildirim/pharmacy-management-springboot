package com.pharmacy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "drug")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Drug {

    @Id
    @Column(length = 50)
    private String barcode;

    @NotBlank
    @Column(length = 200)
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "pres_id")
    private PresType presType;

    @NotNull
    @Min(0)
    @Column(precision = 10, scale = 2)
    private BigDecimal currentSellingPrice;

    @Min(0)
    @Builder.Default
    private Integer minStockAlert = 10;

    @Builder.Default
    private Boolean isActive = true;

    @Version
    private Long version;
}
