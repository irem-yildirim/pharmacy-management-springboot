package com.pharmacy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 100)
    private String name;

    @NotBlank
    @Column(unique = true, length = 50)
    private String username;

    @NotBlank
    @Column(length = 255)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;

    @Builder.Default
    private Boolean isActive = true;
}
