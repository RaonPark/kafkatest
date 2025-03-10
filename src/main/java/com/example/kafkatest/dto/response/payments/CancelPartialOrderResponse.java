package com.example.kafkatest.dto.response.payments;

import com.example.kafkatest.entity.payments.document.Products;
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
