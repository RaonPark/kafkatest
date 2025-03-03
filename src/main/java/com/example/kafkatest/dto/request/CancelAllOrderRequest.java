package com.example.kafkatest.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CancelAllOrderRequest {
    private String orderNumber;
    private boolean instantRefund;
    private String sellerId;
}
