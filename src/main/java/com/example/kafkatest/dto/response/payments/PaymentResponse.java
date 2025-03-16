package com.example.kafkatest.dto.response.payments;

import lombok.Builder;

@Builder
public record PaymentResponse(
        String paymentId,
        long amount,
        boolean completed,
        boolean promoted
) {
}
