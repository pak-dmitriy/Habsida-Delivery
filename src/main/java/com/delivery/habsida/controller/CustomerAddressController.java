package com.delivery.habsida.controller;

import com.delivery.habsida.dto.CustomerAddressCreateRequest;
import com.delivery.habsida.dto.CustomerAddressResponse;
import com.delivery.habsida.service.CustomerAddressService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/customers/{customerId}/addresses")
public class CustomerAddressController {
    private final CustomerAddressService customerAddressService;

    public CustomerAddressController(CustomerAddressService customerAddressService) {
        this.customerAddressService = customerAddressService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CustomerAddressResponse> createAddress(@PathVariable Long customerId,
                                                                 @RequestBody @Valid CustomerAddressCreateRequest request) {
        CustomerAddressResponse customerAddressResponse = customerAddressService.createAddress(customerId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(customerAddressResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<CustomerAddressResponse> getAddresses(@PathVariable Long customerId, @ParameterObject Pageable pageable) {

        return customerAddressService.getAddressesByCustomerId(customerId, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{addressId}")
    public ResponseEntity<CustomerAddressResponse> updateAddress(@RequestBody @Valid CustomerAddressCreateRequest customerAddressCreateRequest,
                                                                 @PathVariable Long customerId,
                                                                 @PathVariable Long addressId) {
        CustomerAddressResponse updateAddress = customerAddressService.updateAddress(customerId, addressId, customerAddressCreateRequest);

        return ResponseEntity.ok(updateAddress);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long customerId, @PathVariable Long addressId) {
        customerAddressService.deleteAddress(customerId, addressId);

        return ResponseEntity.noContent().build();
    }
}
