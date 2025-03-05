package com.example.kafkatest.dto.response;

import lombok.Builder;

@Builder
public record SellerRegisterResponse(
        String sellerId,
        boolean registered
) {
}
