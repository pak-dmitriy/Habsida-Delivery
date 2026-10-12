package com.delivery.habsida.service;

import com.delivery.habsida.dto.CustomerAddressCreateRequest;
import com.delivery.habsida.dto.CustomerAddressResponse;
import com.delivery.habsida.entity.Customer;
import com.delivery.habsida.entity.CustomerAddress;
import com.delivery.habsida.exception.CustomerAddressNotFoundException;
import com.delivery.habsida.exception.CustomerNotFoundException;
import com.delivery.habsida.repository.CustomerAddressRepository;
import com.delivery.habsida.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerAddressService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;

    public CustomerAddressService(CustomerAddressRepository customerAddressRepository, CustomerRepository customerRepository) {
        this.customerAddressRepository = customerAddressRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerAddressResponse createAddress(Long customerId, CustomerAddressCreateRequest customerAddressCreateRequest) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        CustomerAddress customerAddress = new CustomerAddress();
        customerAddress.setCity(customerAddressCreateRequest.city());
        customerAddress.setStreet(customerAddressCreateRequest.street());
        customerAddress.setHouse(customerAddressCreateRequest.house());
        customerAddress.setApartment(customerAddressCreateRequest.apartment());
        customerAddress.setCustomer(customer);

        CustomerAddress savedCustomerAddress = customerAddressRepository.save(customerAddress);

        return CustomerAddressResponse.from(savedCustomerAddress);
    }

    public Page<CustomerAddressResponse> getAddressesByCustomerId(Long customerId, Pageable pageable) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        return customerAddressRepository.findByCustomerId(customerId, pageable)
                .map(CustomerAddressResponse::from);
    }

    @Transactional
    public CustomerAddressResponse updateAddress(Long customerId, Long customerAddressId, CustomerAddressCreateRequest customerAddressCreateRequest) {
        CustomerAddress address = customerAddressRepository.findById(customerAddressId)
                .orElseThrow(() -> new CustomerAddressNotFoundException("Address not found"));
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new CustomerAddressNotFoundException("Customer address not found");
        }

        address.setCity(customerAddressCreateRequest.city());
        address.setStreet(customerAddressCreateRequest.street());
        address.setHouse(customerAddressCreateRequest.house());
        address.setApartment(customerAddressCreateRequest.apartment());

        CustomerAddress updatedCustomerAddress = customerAddressRepository.save(address);

        return CustomerAddressResponse.from(updatedCustomerAddress);
    }

    @Transactional
    public void deleteAddress(Long customerId, Long addressId) {
        CustomerAddress address = customerAddressRepository.findById(addressId)
                .orElseThrow(() -> new CustomerAddressNotFoundException("Address not found"));
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new CustomerAddressNotFoundException("Customer address not found");
        }
        customerAddressRepository.delete(address);
    }
}
