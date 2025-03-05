package com.example.kafkatest.entity.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class TotalRevenue {
    @Id
    private String id;
    private String sellerId;
    private long totalRevenue;

    @Builder
    protected TotalRevenue(String sellerId, long totalRevenue) {
        this.sellerId = sellerId;
        this.totalRevenue = totalRevenue;
    }
}
