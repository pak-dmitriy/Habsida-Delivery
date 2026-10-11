package com.delivery.habsida.service;

import com.delivery.habsida.dto.CustomerCreateRequest;
import com.delivery.habsida.dto.CustomerResponse;
import com.delivery.habsida.dto.CustomerUpdateRequest;
import com.delivery.habsida.entity.Customer;
import com.delivery.habsida.entity.CustomerStatus;
import com.delivery.habsida.exception.CustomerAlreadyExistsException;
import com.delivery.habsida.exception.CustomerNotFoundException;
import com.delivery.habsida.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest customerCreateRequest) {
        String phone = customerCreateRequest.phone();

        Optional<Customer> existCustomer = customerRepository.findByPhone(phone);
        if (existCustomer.isPresent()) {
            throw new CustomerAlreadyExistsException("Customer with phone " + phone + " already exists");
        }

        Customer customer = new Customer();
        customer.setName(customerCreateRequest.name());
        customer.setPhone(phone);
        customer.setStatus(CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.save(customer);

        return CustomerResponse.from(savedCustomer);
    }

    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(CustomerResponse::from);
    }

    public CustomerResponse getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long customerId, CustomerUpdateRequest customerUpdateRequest) {
        String phone = customerUpdateRequest.phone();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        Optional<Customer> existCustomer = customerRepository.findByPhone(phone);
        if (existCustomer.isPresent() && !existCustomer.get().getId().equals(customerId)) {
            throw new CustomerAlreadyExistsException("Customer with phone " + phone + " already exists");
        }

        customer.setName(customerUpdateRequest.name());
        customer.setPhone(phone);
        customer.setStatus(customerUpdateRequest.status());
        Customer updatedCustomer = customerRepository.save(customer);

        return CustomerResponse.from(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        customerRepository.delete(customer);
    }
}
