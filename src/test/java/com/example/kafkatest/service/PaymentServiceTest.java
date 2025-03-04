package com.example.kafkatest.service;

import com.example.kafkatest.dto.request.CancelPaymentRequest;
import com.example.kafkatest.dto.request.PaymentRequest;
import com.example.kafkatest.dto.response.CancelPaymentResponse;
import com.example.kafkatest.dto.response.PaymentResponse;
import com.example.kafkatest.entity.document.Payment;
import com.example.kafkatest.support.PaymentType;
import com.mongodb.client.result.InsertOneResult;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = PaymentService.class)
public class PaymentServiceTest {
    @MockBean
    MongoTemplate mongoTemplate;

    @Autowired
    PaymentService paymentService;

    static final Long PAYMENT_AMOUNT = 922000L;
    static final String PAYMENT_TIMESTAMP = Instant.now().atZone(ZoneId.of("Asia/Seoul")).toString();
    // HEX(TIMESTAMP + AMOUNT)
    static final String PAYMENT_ID = Long.toHexString(Instant.now().toEpochMilli()) + Long.toHexString(PAYMENT_AMOUNT);
    static final String CARD_NUMBER = "0192-3827-2172-1273";
    static final String CARD_CVC = "923";
    static final String CARD_COMPANY = "Raon Card";
    static final String PREVIOUS_PAYMENT_ID = "123123";

    @Test
    void 현금으로_결제하기() {
        // GIVEN
        PaymentRequest payment = PaymentRequest.builder()
                .amount(PAYMENT_AMOUNT)
                .paymentType(PaymentType.CASH)
                .build();

        Payment paymentInfo = Payment.builder()
                .paymentId(PAYMENT_ID)
                .amount(payment.amount())
                .timestamp(PAYMENT_TIMESTAMP)
                .paymentType(payment.paymentType())
                .build();

        // 도큐먼트 id를 하나 생성해준다.
        paymentInfo.setId("1");

        // WHEN
        when(mongoTemplate.save(any(Payment.class))).thenReturn(paymentInfo);
        PaymentResponse resultPayment = paymentService.pay(payment);

        // THEN
        assertTrue(resultPayment.completed());
    }

    @Test
    void 체크카드로_결제하기() {
        // GIVEN
        PaymentRequest payment = PaymentRequest.builder()
                .amount(PAYMENT_AMOUNT)
                .paymentType(PaymentType.DEBIT)
                .cardCompany(CARD_COMPANY)
                .cardNumber(CARD_NUMBER)
                .cardCvc(CARD_CVC)
                .build();

        Payment paymentInfo = Payment.builder()
                .paymentId(PAYMENT_ID)
                .paymentType(payment.paymentType())
                .cardNumber(payment.cardNumber())
                .timestamp(PAYMENT_TIMESTAMP)
                .amount(payment.amount())
                .build();

        paymentInfo.setId("1");

        // when
        when(mongoTemplate.save(any(Payment.class))).thenReturn(paymentInfo);
        PaymentResponse response = paymentService.pay(payment);

        // then
        assertTrue(response.completed());
    }

    @Test
    void 신용카드로_결제하기() {
        // GIVEN
        PaymentRequest payment = PaymentRequest.builder()
                .amount(PAYMENT_AMOUNT)
                .paymentType(PaymentType.CREDIT)
                .cardCompany(CARD_COMPANY)
                .cardNumber(CARD_NUMBER)
                .cardCvc(CARD_CVC)
                .build();

        Payment paymentInfo = Payment.builder()
                .paymentId(PAYMENT_ID)
                .cardNumber(payment.cardNumber())
                .amount(payment.amount())
                .paymentType(payment.paymentType())
                .timestamp(PAYMENT_TIMESTAMP)
                .build();

        paymentInfo.setId("1");

        // WHEN
        when(mongoTemplate.save(any(Payment.class))).thenReturn(paymentInfo);
        PaymentResponse response = paymentService.pay(payment);

        // THEN
        assertTrue(response.completed());
    }

    @Test
    void 신용카드로_결제하고_프로모션_적용() {
        // GIVEN
        PaymentRequest payment = PaymentRequest.builder()
                .amount(PAYMENT_AMOUNT)
                .paymentType(PaymentType.CREDIT)
                .cardCompany(CARD_COMPANY)
                .cardNumber(CARD_NUMBER)
                .cardCvc(CARD_CVC)
                .build();

        Payment paymentInfo = Payment.builder()
                .paymentType(payment.paymentType())
                .amount(payment.amount())
                .cardNumber(payment.cardNumber())
                .paymentId(PAYMENT_ID)
                .timestamp(PAYMENT_TIMESTAMP)
                .build();

        paymentInfo.setId("1");

        // WHEN
        when(mongoTemplate.save(any(Payment.class))).thenReturn(paymentInfo);
        PaymentResponse response = paymentService.pay(payment);

        // THEN
        assertTrue(response.promoted());
        assertTrue(response.completed());
    }

    @Test
    void 현금_환불하기() {
        // GIVEN
        CancelPaymentRequest cancelPayment = CancelPaymentRequest.builder()
                .previousPaymentId(PREVIOUS_PAYMENT_ID)
                .paymentType(PaymentType.CASH)
                .amount(PAYMENT_AMOUNT)
                .build();

        Payment canceledPayment = Payment.builder()
                .paymentId(PAYMENT_ID)
                .paymentType(PaymentType.REFUND)
                .amount(PAYMENT_AMOUNT)
                .timestamp(PAYMENT_TIMESTAMP)
                .build();
        Payment previousPayment = Payment.builder()
                .paymentId(PREVIOUS_PAYMENT_ID)
                .amount(PAYMENT_AMOUNT)
                .timestamp(PAYMENT_TIMESTAMP)
                .paymentType(PaymentType.CASH)
                .build();

        Query findQuery = new Query(Criteria.where("paymentId").is(PREVIOUS_PAYMENT_ID));

        // when
        when(mongoTemplate.save(any(Payment.class))).thenReturn(canceledPayment);
        when(mongoTemplate.findOne(findQuery, Payment.class)).thenReturn(previousPayment);
        CancelPaymentResponse response = paymentService.cancel(cancelPayment);

        // THEN
        assertTrue(response.refund());
        assertEquals(canceledPayment.getPaymentId(), response.refundId());
    }
}
