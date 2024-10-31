package com.example.kafkatest.vo;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MemberVo {
    private String userId;
    private String username;
    private String password;
    private String email;
    private String address;
}
