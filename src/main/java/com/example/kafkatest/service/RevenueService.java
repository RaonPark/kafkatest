package com.example.kafkatest.service;

import com.example.kafkatest.dto.request.TotalRevenueRequest;
import com.example.kafkatest.dto.response.TotalRevenueResponse;
import com.example.kafkatest.entity.document.TotalRevenue;
import com.mongodb.client.result.UpdateResult;
import com.raonpark.RevenueData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class RevenueService {
    private final MongoTemplate mongoTemplate;
    private final RedisService redisService;

    @KafkaListener(topics = {"revenueData"}, groupId = "REVENUE", containerFactory = "revenueDataConcurrentKafkaListenerContainerFactory")
    public void receiveRevenueData(ConsumerRecord<String, RevenueData> record) {
        RevenueData revenueData = record.value();
        TotalRevenueRequest revenueRequest = TotalRevenueRequest.builder()
                .revenue(revenueData.getRevenue())
                .sellerId(revenueData.getSellerId().toString())
                .build();

        modifyTotalRevenue(revenueRequest);

        redisService.incrDelta(revenueData.getOrderNumber().toString(), 1L);

        log.info("revenue = ${} has arrived and accomplished.", revenueData);
    }

    public TotalRevenueResponse modifyTotalRevenue(TotalRevenueRequest revenue) {
        Query findQuery = new Query(Criteria.where("sellerId").is(revenue.sellerId()));
        Update updateQuery = new Update().inc("totalRevenue", revenue.revenue());
        UpdateResult result = mongoTemplate.upsert(findQuery, updateQuery, TotalRevenue.class);

        if(result.getModifiedCount() != 1L)
            throw new RuntimeException("MongoDB 에러!");

        TotalRevenue updatedRevenue = Optional.ofNullable(mongoTemplate.findOne(findQuery, TotalRevenue.class))
                .orElseThrow(() -> new RuntimeException("MongoDB 에러!"));

        return TotalRevenueResponse.builder()
                .totalRevenue(updatedRevenue.getTotalRevenue())
                .build();
    }
}
