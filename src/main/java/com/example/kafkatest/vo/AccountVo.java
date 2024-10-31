package com.example.kafkatest.vo;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AccountVo {
    private String accountNumber;
    private String password;
    private String openDate;
    private Boolean isClosed;
    private BigDecimal balance;
}
