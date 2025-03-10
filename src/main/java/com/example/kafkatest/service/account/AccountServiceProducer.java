package com.example.kafkatest.service.account;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
}
