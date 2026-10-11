package com.delivery.habsida.dto;

import com.delivery.habsida.entity.CustomerAddress;

public record CustomerAddressDTO (
        Long id,
        String city,
        String street,
        String house,
        String apartment,
        Long customerId

){
   public static CustomerAddressDTO from (CustomerAddress customerAddress){
      return  new CustomerAddressDTO (
              customerAddress.getId(),
              customerAddress.getCity(),
              customerAddress.getStreet(),
              customerAddress.getHouse(),
              customerAddress.getApartment(),
              customerAddress.getCustomer().getId()
      );
   }
}
