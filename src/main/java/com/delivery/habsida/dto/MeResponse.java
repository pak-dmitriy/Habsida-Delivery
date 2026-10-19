package com.delivery.habsida.dto;

import java.util.List;

public record MeResponse(
        Long id,
        String email,
        List<String> roles,
        List<MeStoreResponse> stores
) {
}
