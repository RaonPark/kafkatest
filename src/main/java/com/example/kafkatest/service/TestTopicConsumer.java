package com.example.kafkatest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestTopicConsumer {
    @KafkaListener(topics = "testTopic", groupId = "kafkatest")
    public void testMessageConsume(ConsumerRecord<String, String> consumerRecord) {
        log.info("here is consumer. topic = {}, message = {}", consumerRecord.topic(), consumerRecord.value());
    }
}
