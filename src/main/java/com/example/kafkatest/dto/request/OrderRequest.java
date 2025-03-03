package com.example.kafkatest.dto.request;

import com.example.kafkatest.entity.document.Products;
import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String orderNumber;
    private String orderedTime;
    private List<Products> products;
    private String sellerId;
}
