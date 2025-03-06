package com.example.kafkatest.service;

import com.example.kafkatest.dto.request.TotalRevenueRequest;
import com.example.kafkatest.dto.response.TotalRevenueResponse;
import com.example.kafkatest.entity.document.TotalRevenue;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = RevenueService.class)
public class RevenueServiceTest {
    @MockBean
    MongoTemplate mongoTemplate;

    @Autowired
    RevenueService revenueService;

    static final String SELLER_ID = "123456789";
    static final long REVENUE = 928000L;
    @Test
    void 매출액_집계() {
        // GIVEN
        TotalRevenueRequest totalRevenue = TotalRevenueRequest.builder()
                .sellerId(SELLER_ID)
                .revenue(REVENUE)
                .build();

        TotalRevenue savedTotalRevenue = TotalRevenue.builder()
                .sellerId(SELLER_ID)
                .totalRevenue(REVENUE * 100)
                .build();

        Query findQuery = new Query(Criteria.where("sellerId").is(SELLER_ID));
        Update updateQuery = new Update().inc("totalRevenue", REVENUE);

        // WHEN
        when(mongoTemplate.upsert(findQuery, updateQuery, TotalRevenue.class))
                .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
        when(mongoTemplate.findOne(any(Query.class), eq(TotalRevenue.class))).thenReturn(savedTotalRevenue);
        TotalRevenueResponse response = revenueService.modifyTotalRevenue(totalRevenue);

        // THEN
        assertEquals(REVENUE * 100, response.totalRevenue());
    }
}
