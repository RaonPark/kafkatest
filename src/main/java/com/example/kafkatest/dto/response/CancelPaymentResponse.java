package com.example.kafkatest.dto.response;

import lombok.Builder;

@Builder
public record CancelPaymentResponse(
        String refundId,
        boolean refund,
        String timestamp
) {
}
