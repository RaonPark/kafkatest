package com.example.kafkatest.service;

import com.example.Payments;
import com.example.kafkatest.dto.request.PaymentsRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentsService {
    private final KafkaTemplate<String, Payments> paymentsKafkaTemplate;

    @RetryableTopic(attempts = "1",
            kafkaTemplate = "paymentsKafkaTemplate",
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = { "payments" }, groupId = "payments", containerFactory = "paymentsConcurrentKafkaListenerContainerFactory")
    public void handlePayment(Payments payments,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                              @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        log.info("Event on main topic = {}, key = {}, payload = {}", topic, key, payments);

        if(payments.getPaymentsId().toString().contains("error")) {
            throw new RuntimeException("결제 실패!");
        }
    }

    @DltHandler
    public void handleDltPayments(Payments payments,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Event on dlt topic = {}, payload = {}", topic, payments);

        payments.setPaymentsId("retry-payments");

        paymentsKafkaTemplate.send("payments", "retry-payments", payments);
    }

    public void sendPayment(PaymentsRequestDTO payments) {
        log.info("send payments = {}", payments);

        paymentsKafkaTemplate.send("payments", "payments5", convertDto2Avro(payments));
    }

    private Payments convertDto2Avro(PaymentsRequestDTO dto) {
        return Payments.newBuilder()
                .setUser(dto.getUser())
                .setAmount(dto.getAmount())
                .setPaymentsId(dto.getPaymentsId())
                .setCurrency(dto.getCurrency())
                .setIsCredit(dto.isCredit())
                .setPaymentsStamp(Instant.now().toEpochMilli())
                .build();
    }
}
