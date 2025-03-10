package com.example.kafkatest.dto.response.payments;

import lombok.Builder;

@Builder
public record CancelPaymentResponse(
        String refundId,
        boolean refund,
        String timestamp
) {
}
