package com.delivery.habsida.repository;

import com.delivery.habsida.entity.CustomerAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    Page<CustomerAddress> findByCustomerId(Long customerId, Pageable pageable);
}
