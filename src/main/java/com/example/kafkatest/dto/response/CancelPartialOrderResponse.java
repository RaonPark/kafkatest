package com.example.kafkatest.dto.response;

import com.example.kafkatest.entity.document.Products;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelPartialOrderResponse {
    private String orderNumber;
    private List<Products> products;
    private boolean refund;
    private boolean canceled;
    private String sellerId;
}
