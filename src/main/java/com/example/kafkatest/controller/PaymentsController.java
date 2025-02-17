package com.example.kafkatest.controller;

import com.example.Payments;
import com.example.kafkatest.service.PaymentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentsController {
    private final PaymentsService paymentsService;

    @PostMapping("/payments")
    public void payments(@RequestBody Payments payments) {
        paymentsService.sendPayment(payments);
    }
}
