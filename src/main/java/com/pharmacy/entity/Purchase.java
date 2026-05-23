package com.pharmacy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "purchase")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "drug_barcode")
    private Drug drug;

    @NotNull
    @Min(1)
    private Integer originalQuantity;

    @NotNull
    @Min(0)
    private Integer remainingQuantity;

    @NotNull
    @Column(precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @NotNull
    private LocalDate expirationDate;

    @NotNull
    private LocalDate purchaseDate;
}
