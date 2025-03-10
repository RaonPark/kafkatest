package com.example.kafkatest.dto.response.payments;

import lombok.Builder;

@Builder
public record PaymentResponse(
        boolean completed,
        boolean promoted
) {
}
