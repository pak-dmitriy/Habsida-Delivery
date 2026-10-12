package com.delivery.habsida.dto;

import com.delivery.habsida.entity.Store;

public record MeStoreResponse(
        Long id,
        String name
){
   public static MeStoreResponse from(Store store) {
       return new MeStoreResponse(
               store.getId(),
               store.getName()
       );
   }
}
