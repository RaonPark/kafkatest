package com.example.kafkatest.entity.payments.document;

import com.example.kafkatest.support.enums.ProcessStage;
import com.example.kafkatest.support.enums.ProcessType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class OrderOutbox {
    @Id
    private String id;
    private String aggId;
    private ProcessStage processStage;
    private String payload;

    @Builder
    protected OrderOutbox(String aggId, ProcessStage processStage, String payload) {
        this.aggId = aggId;
        this.processStage = processStage;
        this.payload = payload;
    }
}
