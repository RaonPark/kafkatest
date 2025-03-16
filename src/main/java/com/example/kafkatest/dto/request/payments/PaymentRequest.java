package com.example.kafkatest.dto.request.payments;

import com.example.kafkatest.support.enums.PaymentType;
import lombok.Builder;

@Builder
public record PaymentRequest(
        long amount,
        PaymentType paymentType,
        String cardCompany,
        String cardNumber,
        String cardCvc
) { }