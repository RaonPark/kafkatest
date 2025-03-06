package com.example.kafkatest.dto.request;

import lombok.Builder;

@Builder
public record PublishOrderRequest(
        OrderRequest orderRequest,
        PaymentRequest paymentRequest
) {
}
