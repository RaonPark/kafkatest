package com.example.kafkatest.dto.response;

import lombok.Builder;

@Builder
public record PaymentResponse(
        boolean completed,
        boolean promoted
) {
}
