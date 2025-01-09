package com.example.kafkatest.service;

import com.example.ProblemSolving;
import com.example.kafkatest.dto.request.SendSolvingProblemRequest;
import com.example.kafkatest.support.LinearRegressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProblemSolvingService {
    private final KafkaTemplate<String, ProblemSolving> kafkaTemplate;
    private final RedisService redisService;

    public void sendProblemSolving(SendSolvingProblemRequest request) {
        log.info("here's request from producer solvedProblems = {}", request.getSolved());
        kafkaTemplate.send("solving.problem.topic", request.getUserId().toString(),
                ProblemSolving.newBuilder().setUserId(request.getUserId())
                        .setProblemId(request.getProblemId())
                        .setSolved(request.getSolved())
                        .setSolvedTime(System.currentTimeMillis())
                        .build());
    }

    @KafkaListener(topics = "solved.problem.topic", containerFactory = "kafkaListenerContainerFactoryForProblemSolving")
    public void listenProblemSolving(ConsumerRecord<String, Integer> record) {
        redisService.saveList("solvedProblems", record.value());
        redisService.incrOne("solvedCounts");
        log.info("here's savedList = {} and counts = {}",
                redisService.findList("solvedProblems", Integer.class), redisService.find("solvedCounts", Integer.class));
        double rSquared = getRSquaredOfSolvedProblems();
        log.info("here's rSquared = {}", rSquared);
    }

    private double getRSquaredOfSolvedProblems() {
        List<Integer> solvedProblems = redisService.findList("solvedProblems", Integer.class);
        int solvedCounts = redisService.find("solvedCounts", Integer.class);

        List<Double> solvedProblemsForR = solvedProblems.stream().map(integer -> integer * 1.0).toList();
        List<Double> solvedCountsForR = new ArrayList<>();
        for(int i=1; i<=solvedCounts; i++) {
            solvedCountsForR.add(i * 1.0);
        }

        LinearRegressionService linearRegressionService = new LinearRegressionService(solvedCountsForR, solvedProblemsForR);
        return linearRegressionService.getRSquared();
    }
}
