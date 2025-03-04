package com.example.kafkatest.dto.request;

import com.example.kafkatest.support.PaymentType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PaymentRequest(
        long amount,
        PaymentType paymentType,
        String cardCompany,
        String cardNumber,
        String cardCvc
) { }