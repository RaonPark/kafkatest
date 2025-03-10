package com.example.kafkatest.controller.account;

import com.example.kafkatest.dto.MakeAccountDto;
import com.example.kafkatest.dto.request.account.PutMoneyRequest;
import com.example.kafkatest.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/makeAccount")
    public MakeAccountDto.MakeAccountResponse makeAccount(@RequestBody MakeAccountDto.MakeAccountRequest request) {
        return accountService.makeAccount(request);
    }

    @PostMapping("/putMoneyInAccount")
    public String putMoneyInAccount(@RequestBody PutMoneyRequest request) {
        accountService.putMoneyAndProduceMessage(request);
        return "success";
    }
}
