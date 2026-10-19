package com.delivery.habsida.dto;

import com.delivery.habsida.entity.Customer;
import com.delivery.habsida.entity.CustomerStatus;

public record CustomerResponse(
        Long id,
        String name,
        String phone,
        CustomerStatus status

) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getStatus()
        );
    }
}
