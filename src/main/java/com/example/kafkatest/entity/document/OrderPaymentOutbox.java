package com.example.kafkatest.entity.document;

import com.example.kafkatest.support.ProcessedType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class OrderPaymentOutbox {
    @Id
    private String id;
    private String aggId;
    private ProcessedType processedType;
    private String payload;

    @Builder
    protected OrderPaymentOutbox(
            String aggId,
            ProcessedType processedType,
            String payload) {
        this.aggId = aggId;
        this.processedType = processedType;
        this.payload = payload;
    }
}
