package com.example.kafkatest.dto;

import com.example.kafkatest.vo.MemberVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class RegisterDto {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class RegisterRequest {
        private String userId;
        private String username;
        private String password;
        private String email;
        private String address;

        public MemberVo toMemberVo() {
            return MemberVo.builder()
                    .userId(userId)
                    .username(username)
                    .password(password)
                    .email(email)
                    .address(address)
                    .build();
        }
    }
}
