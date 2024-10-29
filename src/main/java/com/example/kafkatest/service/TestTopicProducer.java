package com.example.kafkatest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class TestTopicProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void produceTestMessageToTestTopic() {
        for(int i=0; i<10; i++) {
            kafkaTemplate.send("testTopic", "message " + i + " is coming.").toCompletableFuture()
                    .whenComplete((sendResult, throwable) -> {
                        RecordMetadata metadata = sendResult.getRecordMetadata();
                        log.info("there's topic = {}\n partitions = {}\n, offset = {}\n and timestamp = {}\n",
                                metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp());
                    });
        }
    }
}
