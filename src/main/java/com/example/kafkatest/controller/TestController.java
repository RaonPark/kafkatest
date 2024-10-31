package com.example.kafkatest.controller;

import com.example.kafkatest.dto.MakeAccountDto;
import com.example.kafkatest.dto.request.PutMoneyRequest;
import com.example.kafkatest.entity.Account;
import com.example.kafkatest.entity.Member;
import com.example.kafkatest.repository.MemberRepository;
import com.example.kafkatest.service.AccountService;
import com.example.kafkatest.vo.AccountVo;
import com.example.kafkatest.vo.MemberVo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final MemberRepository memberRepository;
    private final AccountService accountService;
    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping("/integrateTestWithJMeter")
    public String integrate() {
        // given
        String userId = UUID.randomUUID().toString();

        // REGISTER
        MemberVo memberVo = MemberVo.builder()
                .username("test")
                .userId(userId)
                .email("test@naver.com")
                .address("서울특별시")
                .password("1234")
                .build();

        Member member = Member.builder()
                .memberVo(memberVo)
                .build();

        memberRepository.save(member);

        // CREATE ACCOUNT
        String accountNumber = accountService.makeAccount(MakeAccountDto.MakeAccountRequest.builder()
                        .userId(userId)
                        .password("1234")
                .build()).getAccountNumber();

        // SAVING
        accountService.putMoneyAndProduceMessage(PutMoneyRequest.builder()
                        .accountNumber(accountNumber)
                        .money(BigDecimal.valueOf(100000))
                        .nickname("hello")
                        .userId(userId)
                .build());

        return redisTemplate.opsForValue().get("balance");
    }
}
