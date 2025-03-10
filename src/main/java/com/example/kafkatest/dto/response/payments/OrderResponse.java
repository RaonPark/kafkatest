package com.example.kafkatest.dto.response.payments;

import com.example.kafkatest.entity.payments.document.Products;
import com.example.kafkatest.entity.payments.document.ReceiptSellerInfo;
import lombok.*;

import java.util.List;

@Builder
public record OrderResponse(
        String orderNumber,
        List<Products> products,
        String orderedTime,
        ReceiptSellerInfo sellerInfo
) {
}