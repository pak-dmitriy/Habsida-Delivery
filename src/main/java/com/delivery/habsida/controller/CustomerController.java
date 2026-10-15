package com.delivery.habsida.controller;


import com.delivery.habsida.dto.CustomerCreateRequest;
import com.delivery.habsida.dto.CustomerResponse;
import com.delivery.habsida.dto.CustomerUpdateRequest;
import com.delivery.habsida.service.CustomerService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@RequestBody @Valid CustomerCreateRequest customerCreateRequest) {
        CustomerResponse createdCustomerResponse = customerService.createCustomer(customerCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomerResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<CustomerResponse> getAllCustomers(@ParameterObject Pageable pageable) {
        return customerService.getAllCustomers(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{customerId}")
    public CustomerResponse getCustomerById(@PathVariable Long customerId) {
        return customerService.getCustomerById(customerId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(@RequestBody @Valid CustomerUpdateRequest customerUpdateRequest,
                                                           @PathVariable Long customerId) {
        CustomerResponse updatedCustomerResponse = customerService.updateCustomer(customerId, customerUpdateRequest);
        return ResponseEntity.ok(updatedCustomerResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }
}
