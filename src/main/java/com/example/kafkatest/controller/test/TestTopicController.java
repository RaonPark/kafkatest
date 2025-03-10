package com.example.kafkatest.controller.test;

import com.example.kafkatest.service.test.TestTopicConsumer;
import com.example.kafkatest.service.test.TestTopicProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TestTopicController {
    private final TestTopicProducer testTopicProducer;
    private final TestTopicConsumer testTopicConsumer;

    @GetMapping("/produce")
    public void produce() {
        testTopicProducer.produceTestMessageToTestTopic();
        log.info("message produced");
    }
}
