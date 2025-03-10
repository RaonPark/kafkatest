package com.example.kafkatest.dto.response.payments;

import lombok.Builder;

@Builder
public record TotalRevenueResponse(
        long totalRevenue
) {
}
