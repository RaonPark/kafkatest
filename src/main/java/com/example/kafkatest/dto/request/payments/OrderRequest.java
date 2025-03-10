package com.example.kafkatest.dto.request.payments;

import com.example.kafkatest.entity.payments.document.Products;
import lombok.*;

import java.util.List;

@Builder
public record OrderRequest(
        List<Products> products,
        String sellerId
) {

}
