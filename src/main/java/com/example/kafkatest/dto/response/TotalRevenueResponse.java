package com.example.kafkatest.dto.response;

import lombok.Builder;

@Builder
public record TotalRevenueResponse(
        long totalRevenue
) {
}
