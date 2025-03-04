package com.example.kafkatest.entity.document;

import com.example.kafkatest.support.PaymentType;
import com.mongodb.lang.Nullable;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Payment {
    @Id
    private String id;
    private String paymentId;
    private long amount;
    private String timestamp;
    private PaymentType paymentType;
    @Nullable
    private String cardNumber;

    @Builder
    private Payment(
            String paymentId,
            long amount,
            String timestamp,
            PaymentType paymentType,
            @Nullable String cardNumber) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.paymentType = paymentType;
        this.cardNumber = cardNumber;
    }
}
