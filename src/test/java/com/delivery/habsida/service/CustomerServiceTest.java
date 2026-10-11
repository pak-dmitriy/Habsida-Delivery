package com.delivery.habsida.service;

import com.delivery.habsida.dto.CustomerCreateRequest;
import com.delivery.habsida.dto.CustomerResponse;
import com.delivery.habsida.dto.CustomerUpdateRequest;
import com.delivery.habsida.entity.Customer;
import com.delivery.habsida.entity.CustomerStatus;
import com.delivery.habsida.exception.CustomerAlreadyExistsException;
import com.delivery.habsida.exception.CustomerNotFoundException;
import com.delivery.habsida.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer defaultCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("name");
        customer.setPhone("123456");
        customer.setStatus(CustomerStatus.ACTIVE);

        return customer;
    }

    private CustomerCreateRequest defaultRequest() {
        return new CustomerCreateRequest(
                "name",
                "123456"
        );
    }

    @Test
    void createCustomer_success() {
        when(customerRepository.findByPhone("123456")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.createCustomer(defaultRequest());

        assertEquals("name", response.name());
        assertEquals("123456", response.phone());
        assertEquals(CustomerStatus.ACTIVE, response.status());

        verify(customerRepository).save(any());
        verify(customerRepository).findByPhone("123456");
    }

    @Test
    void createCustomer_shouldThrowException_whenPhoneAlreadyExists() {
        when(customerRepository.findByPhone(defaultCustomer().getPhone())).thenReturn(Optional.of(defaultCustomer()));

        assertThrows(CustomerAlreadyExistsException.class,
                () -> customerService.createCustomer(defaultRequest()));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void getAllCustomers_success() {
        Pageable pageable = PageRequest.of(0, 10);

        when(customerRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(defaultCustomer())));

        Page<CustomerResponse> page = customerService.getAllCustomers(pageable);
        CustomerResponse response = page.getContent().get(0);

        assertEquals(1, page.getContent().size());
        assertEquals("name", response.name());
        assertEquals("123456", response.phone());
        assertEquals(CustomerStatus.ACTIVE, response.status());

        verify(customerRepository).findAll(pageable);
    }

    @Test
    void getCustomerById_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(defaultCustomer()));

        CustomerResponse response = customerService.getCustomerById(1L);

        assertEquals(1L, response.id());
        assertEquals("name", response.name());
        assertEquals("123456", response.phone());
        assertEquals(CustomerStatus.ACTIVE, response.status());

        verify(customerRepository).findById(1L);
    }

    @Test
    void getCustomerById_notFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(99L));

        verify(customerRepository).findById(99L);
    }

    @Test
    void updateCustomer_success() {
        CustomerUpdateRequest request = new CustomerUpdateRequest("Dima", "222222", CustomerStatus.BLOCKED);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(defaultCustomer()));
        when(customerRepository.findByPhone("222222")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));


        CustomerResponse updateCustomer = customerService.updateCustomer(1L, request);

        assertEquals(1L, updateCustomer.id());
        assertEquals("Dima", updateCustomer.name());
        assertEquals("222222", updateCustomer.phone());
        assertEquals(CustomerStatus.BLOCKED, updateCustomer.status());

        verify(customerRepository).save(any());
        verify(customerRepository).findByPhone("222222");
    }

    @Test
    void updateCustomer_shouldThrowException_whenPhoneBelongsToDifferentCustomer() {
        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);
        otherCustomer.setPhone("999999");

        CustomerUpdateRequest request = new CustomerUpdateRequest("Dima", "999999", CustomerStatus.ACTIVE);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(defaultCustomer()));
        when(customerRepository.findByPhone("999999")).thenReturn(Optional.of(otherCustomer));

        assertThrows(CustomerAlreadyExistsException.class,
                () -> customerService.updateCustomer(1L, request));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void deleteCustomer_success() {
        Customer customer = defaultCustomer();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(customer);
    }

    @Test
    void deleteCustomer_shouldThrowException_whenCustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.deleteCustomer(1L));

        verify(customerRepository, never()).delete(any());
    }
}