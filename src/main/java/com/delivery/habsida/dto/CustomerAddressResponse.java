package com.delivery.habsida.dto;

import com.delivery.habsida.entity.CustomerAddress;

public record CustomerAddressResponse(
        Long id,
        String city,
        String street,
        String house,
        String apartment,
        Long customerId

){
   public static CustomerAddressResponse from (CustomerAddress customerAddress){
      return  new CustomerAddressResponse(
              customerAddress.getId(),
              customerAddress.getCity(),
              customerAddress.getStreet(),
              customerAddress.getHouse(),
              customerAddress.getApartment(),
              customerAddress.getCustomer().getId()
      );
   }
}
