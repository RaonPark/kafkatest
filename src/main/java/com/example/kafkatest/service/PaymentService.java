package com.example.kafkatest.service;

import com.example.kafkatest.dto.request.CancelPaymentRequest;
import com.example.kafkatest.dto.request.PaymentRequest;
import com.example.kafkatest.dto.response.CancelPaymentResponse;
import com.example.kafkatest.dto.response.PaymentResponse;
import com.example.kafkatest.entity.document.Payment;
import com.example.kafkatest.support.PaymentType;
import com.raonpark.PaymentData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, PaymentData> paymentDataKafkaTemplate;
    private final RedisService redisService;

    @KafkaListener(topics = {"paymentData"}, groupId = "PAYMENT", containerFactory = "paymentDataConcurrentKafkaListenerContainerFactory")
    public void consumePayment(ConsumerRecord<String, PaymentData> record) {
        PaymentData paymentData = record.value();
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .paymentType(PaymentType.findType(paymentData.getPaymentType().toString()))
                .amount(paymentData.getAmount())
                .cardCompany(paymentData.getCardCompany().toString())
                .cardCvc(paymentData.getCardCvc().toString())
                .cardNumber(paymentData.getCardNumber().toString())
                .build();

        CompletableFuture<PaymentResponse> paymentFuture = CompletableFuture.supplyAsync(() -> pay(paymentRequest))
                        .thenApply(paymentResponse -> {
                            redisService.incrDelta(paymentData.getOrderNumber().toString(), 10);
                            return paymentResponse;
                        });

        PaymentResponse response = paymentFuture.join();

        log.info("payment = ${} has arrived and accomplished.", response);
    }

    public PaymentResponse pay(PaymentRequest payment) {
        PaymentResponse response = null;
        if(payment.paymentType() == PaymentType.CASH) {
            response = payWithCash(payment);
        } else if(payment.paymentType() == PaymentType.DEBIT || payment.paymentType() == PaymentType.CREDIT) {
            response = payWithCard(payment);
        }

        return response;
    }

    private PaymentResponse payWithCash(PaymentRequest payment) {
        Payment cashPayment = Payment.builder()
                .paymentId(generatePaymentId(payment.amount()))
                .timestamp(Instant.now().atZone(ZoneId.of("Asia/Seoul")).toString())
                .amount(payment.amount())
                .paymentType(payment.paymentType())
                .build();

        Payment savedPayment = mongoTemplate.save(cashPayment);

        if(savedPayment.getId() == null)
            throw new RuntimeException("MongoDB 에러 발생!");

        return PaymentResponse.builder()
                .promoted(false)
                .completed(true)
                .build();
    }

    private PaymentResponse payWithCard(PaymentRequest payment) {
        Payment cardPayment = Payment.builder()
                .paymentId(generatePaymentId(payment.amount()))
                .amount(payment.amount())
                .timestamp(Instant.now().atZone(ZoneId.of("Asia/Seoul")).toString())
                .cardNumber(payment.cardNumber())
                .build();

        Payment savedPayment = mongoTemplate.save(cardPayment);

        if(savedPayment.getPaymentId() == null) {
            throw new RuntimeException("MongoDB 에러 발생!");
        }

        return PaymentResponse.builder()
                .completed(true)
                .promoted(true)
                .build();
    }

    private String generatePaymentId(long amount) {
        long timestamp = Instant.now().toEpochMilli();
        return Long.toHexString(timestamp) + Long.toHexString(amount);
    }

    public CancelPaymentResponse cancel(CancelPaymentRequest cancelPayment) {
        Query findQuery = new Query(Criteria.where("paymentId").is(cancelPayment.previousPaymentId()));
        Payment previousPayment = Optional.ofNullable(mongoTemplate.findOne(findQuery, Payment.class))
                .orElseThrow(() -> new RuntimeException(cancelPayment.previousPaymentId() + " 에 해당하는 결제 정보를 찾을 수 없음."));

        if(cancelPayment.amount() > previousPayment.getAmount()) {
            throw new RuntimeException("이전 결제보다 더 많은 돈을 환불할 수 없습니다.");
        }

        Payment refundPayment = Payment.builder()
                .paymentType(cancelPayment.paymentType())
                .cardNumber(previousPayment.getCardNumber())
                .timestamp(Instant.now().atZone(ZoneId.of("Asia/Seoul")).toString())
                .amount(cancelPayment.amount())
                .paymentId(generatePaymentId(cancelPayment.amount()))
                .build();

        Payment savedRefund = Optional.of(mongoTemplate.save(refundPayment))
                .orElseThrow(() -> new RuntimeException("에러 발생!"));

        return CancelPaymentResponse.builder()
                .refund(true)
                .refundId(savedRefund.getPaymentId())
                .build();
    }
}
