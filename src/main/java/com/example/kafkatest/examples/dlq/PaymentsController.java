package com.example.kafkatest.examples.dlq;

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
    public void payments(@RequestBody PaymentsRequestDTO payments) {
        paymentsService.sendPayment(payments);
    }

    @PostMapping("/paymentsStreams")
    public void paymentsStreams(@RequestBody PaymentsRequestDTO payments) {
        paymentsService.sendPaymentsToStream(payments);
    }
}
