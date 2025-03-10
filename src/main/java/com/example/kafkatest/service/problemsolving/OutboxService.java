package com.example.kafkatest.service.problemsolving;

import com.example.kafkatest.entity.payments.document.OrderPaymentOutbox;
import com.example.kafkatest.support.ProcessedType;
import com.raonpark.OrderPaymentOutboxAvro;
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
    private final MongoTemplate outboxPoller;

    public List<OrderPaymentOutbox> pollingOutbox() {
        Query findQuery = new Query(Criteria.where("processedType").in(ProcessedType.ORDER, ProcessedType.PAYMENT));
        return outboxPoller.find(findQuery, OrderPaymentOutbox.class);
    }

    public void publishingOutbox(OrderPaymentOutbox outbox) {
        OrderPaymentOutboxAvro avro = OrderPaymentOutboxAvro.newBuilder()
                .setAggId(String.valueOf(outbox.getAggId()))
                .setPayload(outbox.getPayload())
                .setProcessStage(outbox.getProcessedType().getStage())
                .build();

        if(outbox.getProcessedType().equals(ProcessedType.ORDER)) {
            outboxPublisher.send("order-payment-outbox.topic", String.valueOf(outbox.getAggId()), avro);
        } else if(outbox.getProcessedType().equals(ProcessedType.PAYMENT)) {
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
}
