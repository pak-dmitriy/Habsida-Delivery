package com.delivery.habsida.service;

import com.delivery.habsida.dto.CustomerAddressCreateRequest;
import com.delivery.habsida.dto.CustomerAddressResponse;
import com.delivery.habsida.entity.Customer;
import com.delivery.habsida.entity.CustomerAddress;
import com.delivery.habsida.exception.CustomerAddressNotFoundException;
import com.delivery.habsida.exception.CustomerNotFoundException;
import com.delivery.habsida.repository.CustomerAddressRepository;
import com.delivery.habsida.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
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
class CustomerAddressServiceTest {
    @Mock
    private CustomerAddressRepository customerAddressRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerAddressService customerAddressService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
    }

    private CustomerAddressCreateRequest defaultRequest() {
        return new CustomerAddressCreateRequest(
                "city",
                "street",
                "house",
                "apartment"
        );
    }

    private CustomerAddress defaultAddress() {
        CustomerAddress address = new CustomerAddress();
        address.setId(5L);
        address.setCity("city");
        address.setStreet("street");
        address.setHouse("house");
        address.setApartment("apartment");
        address.setCustomer(customer);

        return address;
    }

    @Test
    void createAddress_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.save(any(CustomerAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerAddressResponse response = customerAddressService.createAddress(1L, defaultRequest());

        assertEquals("city", response.city());
        assertEquals("street", response.street());
        assertEquals("house", response.house());
        assertEquals("apartment", response.apartment());
        assertEquals(1L, response.customerId());

        verify(customerRepository).findById(1L);
        verify(customerAddressRepository).save(any());
    }

    @Test
    void createAddress_shouldThrowException_whenCustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerAddressService.createAddress(1L, defaultRequest()));

        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    void getAddressesByCustomerId_success() {
        Pageable pageable = PageRequest.of(0, 10);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerAddressRepository.findByCustomerId(1L, pageable)).thenReturn(new PageImpl<>(List.of(defaultAddress())));

        Page<CustomerAddressResponse> page = customerAddressService.getAddressesByCustomerId(1L, pageable);
        CustomerAddressResponse response = page.getContent().get(0);

        assertEquals(1, page.getContent().size());
        assertEquals(5L, response.id());
        assertEquals("city", response.city());
        assertEquals("street", response.street());
        assertEquals("house", response.house());
        assertEquals("apartment", response.apartment());
        assertEquals(1L, response.customerId());

        verify(customerRepository).findById(1L);
        verify(customerAddressRepository).findByCustomerId(eq(1L), eq(pageable));
    }

    @Test
    void getAddressesByCustomerId_shouldThrowException_whenCustomerNotFoud() {
        Pageable pageable = PageRequest.of(0, 10);

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerAddressService.getAddressesByCustomerId(1L, pageable));

        verify(customerRepository).findById(1L);
        verify(customerAddressRepository, never()).findByCustomerId(anyLong(), any());
    }

    @Test
    void updateAddress() {
        CustomerAddressCreateRequest request = new CustomerAddressCreateRequest(
                "Gwangju",
                "Luna",
                "21",
                "2"
        );

        CustomerAddress address = defaultAddress();

        when(customerAddressRepository.findById(5L)).thenReturn(Optional.of(address));
        when(customerAddressRepository.save(any(CustomerAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerAddressResponse updateAddress = customerAddressService.updateAddress(1L, 5L, request);

        assertEquals(5L, updateAddress.id());
        assertEquals("Gwangju", updateAddress.city());
        assertEquals("Luna", updateAddress.street());
        assertEquals("21", updateAddress.house());
        assertEquals("2", updateAddress.apartment());
        assertEquals(1L, updateAddress.customerId());

        verify(customerAddressRepository).save(address);
    }

    @Test
    void updateAddress_shouldThrowException_whenAddressBelongsToDifferentCustomer() {
        CustomerAddressCreateRequest request = new CustomerAddressCreateRequest(
                "London",
                "Luna",
                "21",
                "2");

        when(customerAddressRepository.findById(5L)).thenReturn(Optional.of(defaultAddress()));

        assertThrows(CustomerAddressNotFoundException.class,
                () -> customerAddressService.updateAddress(99L, 5L, request));

        verify(customerAddressRepository).findById(5L);
        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    void updateAddress_shouldThrowException_whenAddressNotFound() {
        when(customerAddressRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(CustomerAddressNotFoundException.class,
                () -> customerAddressService.updateAddress(1L, 5L, defaultRequest()));

        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    void deleteAddress_success() {
        CustomerAddress address = defaultAddress();

        when(customerAddressRepository.findById(5L)).thenReturn(Optional.of(address));

        customerAddressService.deleteAddress(1L, 5L);

        verify(customerAddressRepository).delete(address);
    }

    @Test
    void deleteAddress_shouldThrowException_whenAddressNotFound() {
        when(customerAddressRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(CustomerAddressNotFoundException.class,
                () -> customerAddressService.deleteAddress(1L, 5L));

        verify(customerAddressRepository, never()).delete(any());
    }

    @Test
    void deleteAddress_shouldThrowException_whenAddressBelongsToDifferentCustomer() {
        when(customerAddressRepository.findById(5L)).thenReturn(Optional.of(defaultAddress()));

        assertThrows(CustomerAddressNotFoundException.class,
                () -> customerAddressService.deleteAddress(99L, 5L));

        verify(customerAddressRepository, never()).delete(any());
    }
}