package com.example.kafkatest.dto.response;

import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CancelAllOrderResponse {
    private String orderNumber;
    private boolean refund;
    private boolean canceled;
}
