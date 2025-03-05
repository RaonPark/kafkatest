package com.example.kafkatest.entity.document;

import lombok.Builder;

@Builder
public record ReceiptSellerInfo(
        String businessName,
        String address,
        String telephone
) {
}
