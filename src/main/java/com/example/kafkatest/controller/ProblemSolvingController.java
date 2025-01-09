package com.example.kafkatest.controller;

import com.example.kafkatest.dto.request.SendSolvingProblemRequest;
import com.example.kafkatest.service.ProblemSolvingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProblemSolvingController {
    private final ProblemSolvingService problemSolvingService;

    @PostMapping("/problemSolving")
    public void problemSolving(@RequestBody SendSolvingProblemRequest request) {
        problemSolvingService.sendProblemSolving(request);
    }
}
