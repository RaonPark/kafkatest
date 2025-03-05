package com.example.kafkatest.dto.response;

import com.example.kafkatest.entity.document.Products;
import com.example.kafkatest.entity.document.ReceiptSellerInfo;
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