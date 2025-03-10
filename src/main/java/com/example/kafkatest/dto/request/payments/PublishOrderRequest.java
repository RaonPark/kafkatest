package com.example.kafkatest.dto.request.payments;

import lombok.Builder;

@Builder
public record PublishOrderRequest(
        OrderRequest orderRequest,
        PaymentRequest paymentRequest
) {
}
