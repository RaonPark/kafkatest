package com.example.kafkatest.dto.request;

import lombok.Builder;

@Builder
public record TotalRevenueRequest (
        String sellerId,
        long revenue
) {
}
