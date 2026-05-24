package com.pharmacy.dto.response;

import com.pharmacy.model.Customer;
import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Long id;
    private String name;
    private String phone;
    private BigDecimal balance;
    private Boolean isActive;

    public static CustomerResponse fromEntity(Customer customer) {
        if (customer == null) {
            return null;
        }
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .balance(customer.getBalance())
                .isActive(customer.getIsActive())
                .build();
    }
}
