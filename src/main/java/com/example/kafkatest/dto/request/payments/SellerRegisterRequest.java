package com.example.kafkatest.dto.request.payments;

import lombok.Builder;

@Builder
public record SellerRegisterRequest(
        String businessName,
        String address,
        String accountNumber,
        String telephone
) {
}
