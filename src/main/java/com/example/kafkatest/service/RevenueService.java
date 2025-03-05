package com.example.kafkatest.service;

import com.example.kafkatest.dto.request.TotalRevenueRequest;
import com.example.kafkatest.dto.response.TotalRevenueResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RevenueService {
    private final MongoTemplate mongoTemplate;

    public TotalRevenueResponse modifyTotalRevenue(TotalRevenueRequest revenue) {
        return TotalRevenueResponse.builder()
                .totalRevenue(revenue.revenue())
                .build();
    }
}
