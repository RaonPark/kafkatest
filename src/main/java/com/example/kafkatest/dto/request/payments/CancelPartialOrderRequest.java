package com.example.kafkatest.dto.request.payments;

import com.example.kafkatest.entity.payments.document.Products;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CancelPartialOrderRequest {
    private String orderNumber;
    private List<Products> productsToCancel;
    private boolean instantRefund;
    private String sellerId;
}
