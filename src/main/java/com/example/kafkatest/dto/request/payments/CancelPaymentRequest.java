package com.example.kafkatest.dto.request.payments;

import com.example.kafkatest.support.PaymentType;
import lombok.Builder;

@Builder
public record CancelPaymentRequest(
        String previousPaymentId,
        long amount,
        PaymentType paymentType
) {
}
