package com.example.kafkatest.service.payments_service;

import com.example.kafkatest.entity.payments.document.OrderOutbox;
import com.example.kafkatest.entity.payments.document.OrderPaymentOutbox;
import com.example.kafkatest.entity.payments.document.PaymentOutbox;
import com.example.kafkatest.support.enums.ProcessStage;
import com.example.kafkatest.support.enums.ProcessType;
import com.raonpark.OrderPaymentOutboxAvro;
import com.raonpark.avro.OrderOutboxMessage;
import com.raonpark.avro.PaymentOutboxMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxService {
    private final KafkaTemplate<String, OrderPaymentOutboxAvro> outboxPublisher;
    private final KafkaTemplate<String, OrderOutboxMessage> orderOutboxMessagePublisher;
    private final KafkaTemplate<String, PaymentOutboxMessage> paymentOutboxMessagePublisher;
    private final MongoTemplate outboxPoller;

    public List<OrderPaymentOutbox> pollingOutbox() {
        Query findQuery = new Query(Criteria.where("processedType").in(ProcessType.ORDER, ProcessType.PAYMENT));
        return outboxPoller.find(findQuery, OrderPaymentOutbox.class);
    }

    public void publishingOutbox(OrderPaymentOutbox outbox) {
        OrderPaymentOutboxAvro avro = OrderPaymentOutboxAvro.newBuilder()
                .setAggId(String.valueOf(outbox.getAggId()))
                .setPayload(outbox.getPayload())
                .setProcessStage(outbox.getProcessType().getStage())
                .build();

        if(outbox.getProcessType().equals(ProcessType.ORDER)) {
            outboxPublisher.send("order-payment-outbox.topic", String.valueOf(outbox.getAggId()), avro);
        } else if(outbox.getProcessType().equals(ProcessType.PAYMENT)) {
            outboxPublisher.send("order-payment-outbox.topic", String.valueOf(outbox.getAggId()), avro);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void processOutbox() {
        List<OrderPaymentOutbox> outboxes = pollingOutbox();

        for(OrderPaymentOutbox outbox : outboxes) {
            try {
                log.info("outbox publishing = {}", outbox);

                publishingOutbox(outbox);

                outboxPoller.save(outbox);
            } catch (Exception e) {
                log.error("Outbox Error!");
            }
        }
    }

    public List<OrderOutbox> pollingOrderOutbox() {
        Query findQuery = new Query(Criteria.where("processStage").is(ProcessStage.PROCESSED));
        return outboxPoller.find(findQuery, OrderOutbox.class);
    }

    public void publishingOrderOutbox(OrderOutbox orderOutbox) {
        OrderOutboxMessage message = OrderOutboxMessage.newBuilder()
                .setAggId(orderOutbox.getAggId())
                .setProcessStage(orderOutbox.getProcessStage().getStage())
                .setPayload(orderOutbox.getPayload())
                .build();

        // outbox 에서 payment 정보를 받아서 payload 에 붙여서 던진다.
        // 문제는 어떻게 payment 정보를 받을것인가?
        // https://d2.naver.com/helloworld/9581727
        // 클라이언트에서 동시에 order 정보와 payment 정보를 저장한다.
        // 그 다음에 order가 처리되면 order outbox 에
        // payment가 처리되면 payment outbox 에 저장해야한다.
        // 이게 transactional outbox pattern 이다.

        orderOutboxMessagePublisher.send("order-outbox.topic", orderOutbox.getAggId(), message);
    }

    public List<PaymentOutbox> pollingPaymentOutbox() {
        Query findQuery = new Query(Criteria.where("processStage").is(ProcessStage.PENDING));
        return outboxPoller.find(findQuery, PaymentOutbox.class);
    }

    public void publishingPaymentOutbox(PaymentOutbox paymentOutbox) {
        PaymentOutboxMessage message = PaymentOutboxMessage.newBuilder()
                .setAggId(paymentOutbox.getAggId())
                .setPayload(paymentOutbox.getPayload())
                .setProcessStage(paymentOutbox.getProcessStage().getStage())
                .build();

        paymentOutboxMessagePublisher.send("payment-outbox.topic", paymentOutbox.getAggId(), message);
    }

    @Scheduled(fixedRate = 1000L)
    public void processOrder() {
        List<OrderOutbox> orderOutboxes = pollingOrderOutbox();

        for(OrderOutbox orderOutbox: orderOutboxes) {
            orderOutbox.setProcessStage(ProcessStage.PROCESSED);
            publishingOrderOutbox(orderOutbox);
            outboxPoller.save(orderOutbox);
        }
    }
}
