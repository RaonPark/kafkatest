package com.example.kafkatest.dto.response.payments;

import lombok.Builder;

@Builder
public record SellerRegisterResponse(
        String sellerId,
        boolean registered
) {
}
