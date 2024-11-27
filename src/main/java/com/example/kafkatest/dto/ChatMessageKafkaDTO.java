package com.example.kafkatest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageKafkaDTO {
    private String message;
    private int order;

    @Builder
    protected ChatMessageKafkaDTO(String message, int order) {
        this.message = message;
        this.order = order;
    }
}
