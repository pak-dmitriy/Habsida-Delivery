package com.delivery.habsida.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoreDeliveryRestrictionRequest(

        @NotBlank(message = "Restricted area can not be empty")
        @Size(max = 100, message = "Restricted area can not be longer than 100 characters")
        String restrictedArea
) {
    public StoreDeliveryRestrictionRequest {
        if (restrictedArea != null) {
            restrictedArea = restrictedArea.strip();
        }
    }
}
