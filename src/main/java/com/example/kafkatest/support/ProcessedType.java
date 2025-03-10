package com.example.kafkatest.support;

import java.util.Arrays;
import java.util.Optional;

public enum ProcessedType {
    NOT_PROCESSED("처리전"),
    ORDER("주문처리"),
    PAYMENT("결제"),
    TOTAL_REVENUE("매출액"),
    NOOP("예외발생");

    private final String type;

    ProcessedType(String type) {
        this.type = type;
    }

    public String getStage() {
        return type;
    }

    public static ProcessedType toStage(String type) {
        return Arrays.stream(ProcessedType.values())
                .filter(processedType -> processedType.getStage().equals(type))
                .findAny().orElse(ProcessedType.NOOP);
    }
}
