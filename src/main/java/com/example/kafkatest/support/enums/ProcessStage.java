package com.example.kafkatest.support.enums;

import java.util.Arrays;

public enum ProcessStage {
    BEFORE("처리전"),
    PENDING("처리중"),
    PROCESSED("처리됨"),
    EXCEPTION("예외발생"),
    NO_OP("정보없음");

    private final String stage;
    ProcessStage(String stage) {
        this.stage = stage;
    }

    public String getStage() {
        return stage;
    }

    public static ProcessStage findProcessStage(String stage) {
        return Arrays.stream(ProcessStage.values())
                .filter((processStage) -> processStage.stage.equals(stage))
                .findFirst()
                .orElse(ProcessStage.NO_OP);
    }
}
