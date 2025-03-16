package com.example.kafkatest.support.enums;

import java.util.Arrays;

public enum ProcessType {
    NOT_PROCESSED("처리전"),
    ORDER("주문처리"),
    PAYMENT("결제"),
    TOTAL_REVENUE("매출액"),
    NOOP("예외발생");

    private final String type;

    ProcessType(String type) {
        this.type = type;
    }

    public String getStage() {
        return type;
    }

    public static ProcessType toStage(String type) {
        return Arrays.stream(ProcessType.values())
                .filter(processedType -> processedType.getStage().equals(type))
                .findAny().orElse(ProcessType.NOOP);
    }
}
