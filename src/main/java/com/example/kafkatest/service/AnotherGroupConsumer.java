package com.example.kafkatest.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AnotherGroupConsumer {
    @KafkaListener(topics = "testTopic", groupId = "anotherId")
    public void consume(ConsumerRecord<String, String> consumerRecord) {
        log.info("there is another group's testTopic.\n topic = {}\n value = {}\n",
                consumerRecord.topic(), consumerRecord.value());
    }
}
