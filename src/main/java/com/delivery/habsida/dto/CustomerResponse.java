package com.delivery.habsida.dto;

import com.delivery.habsida.entity.Customer;
import com.delivery.habsida.entity.CustomerStatus;

public record CustomerDTO(
        Long id,
        String name,
        String phone,
        CustomerStatus status

) {
    public static CustomerDTO from(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getStatus()
        );
    }
}
