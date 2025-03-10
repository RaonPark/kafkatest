package com.example.kafkatest.dto.request;

import com.example.kafkatest.entity.document.Products;
import lombok.*;

import java.util.List;

@Builder
public record OrderRequest(
        List<Products> products,
        String sellerId
) {

}
