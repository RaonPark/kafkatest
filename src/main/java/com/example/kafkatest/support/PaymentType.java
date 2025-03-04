package com.example.kafkatest.support;

public enum PaymentType {
    CASH("CASH"),
    DEBIT("DEBIT"),
    CREDIT("CREDIT"),
    REFUND("REFUND");

    private final String type;

    PaymentType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
