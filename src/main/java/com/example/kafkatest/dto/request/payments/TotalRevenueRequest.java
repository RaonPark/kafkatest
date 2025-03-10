package com.example.kafkatest.dto.request.payments;

import lombok.Builder;

@Builder
public record TotalRevenueRequest (
        String sellerId,
        long revenue
) {
}
