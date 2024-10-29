package com.example.kafkatest.controller;

import com.example.kafkatest.service.TestTopicConsumer;
import com.example.kafkatest.service.TestTopicProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
