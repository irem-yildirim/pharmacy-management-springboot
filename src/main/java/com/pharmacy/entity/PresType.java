package com.pharmacy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "pres_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 50)
    private String name;

    @Min(0)
    private Integer riskLevel;
}
