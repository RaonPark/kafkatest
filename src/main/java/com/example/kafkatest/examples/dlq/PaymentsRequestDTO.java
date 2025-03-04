package com.example.kafkatest.examples.dlq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
public class PaymentsRequestDTO {
    private final long user;
    private final double amount;
    private final String currency;
    private final LocalDateTime paymentsStamp;
    private final boolean isCredit;
    private final String paymentsId;
}
