package com.example.kafkatest.support;

import java.util.Arrays;

public enum PaymentType {
    CASH("CASH"),
    DEBIT("DEBIT"),
    CREDIT("CREDIT"),
    REFUND("REFUND"),
    NOOP("NOOP");

    private final String type;

    PaymentType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    public static PaymentType findType(String type) {
        return Arrays.stream(PaymentType.values())
                .filter(paymentType -> paymentType.type.equals(type))
                .findAny()
                .orElse(NOOP);
    }
}
