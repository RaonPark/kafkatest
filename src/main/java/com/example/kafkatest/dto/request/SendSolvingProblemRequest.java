package com.example.kafkatest.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendSolvingProblemRequest {
    private Long userId;
    private Long problemId;
    private int solved;
}
