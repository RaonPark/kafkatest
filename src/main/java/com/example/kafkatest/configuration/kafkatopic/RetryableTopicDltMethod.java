package com.example.kafkatest.configuration.kafkatopic;

import com.example.Payments;
import com.example.kafkatest.configuration.properties.KafkaTopicNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetryableTopicDltMethod {
    private final KafkaTemplate<String, Payments> kafkaTemplate;

    public void sendErrorPaymentsToDlq(Payments payments) {
        log.info("log for dlt = {}", payments);
        kafkaTemplate.send(KafkaTopicNames.PAYMENTS_STREAMS_DLQ_TOPIC, "error-payments", payments);
    }
}
