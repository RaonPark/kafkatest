package com.example.kafkatest.dto;

import com.example.kafkatest.vo.AccountVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class MakeAccountDto {
    @Builder
    @Getter
    @AllArgsConstructor
    public static class MakeAccountRequest {
        private String userId;
        private String password;

        public AccountVo toAccountVo() {
            return AccountVo.builder()
                    .password(password)
                    .build();
        }
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class MakeAccountResponse {
        private String accountNumber;
    }
}
