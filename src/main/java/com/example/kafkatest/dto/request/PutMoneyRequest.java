package com.example.kafkatest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PutMoneyRequest {
    private String accountNumber;
    private String userId;
    private String nickname;
    private BigDecimal money;
}
