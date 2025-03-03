package com.example.kafkatest.service;

import com.example.kafkatest.dto.request.CancelAllOrderRequest;
import com.example.kafkatest.dto.request.CancelPartialOrderRequest;
import com.example.kafkatest.dto.request.OrderRequest;
import com.example.kafkatest.dto.response.CancelAllOrderResponse;
import com.example.kafkatest.dto.response.CancelPartialOrderResponse;
import com.example.kafkatest.dto.response.OrderResponse;
import com.example.kafkatest.entity.document.Orders;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final MongoTemplate mongoTemplate;

    public OrderResponse publishOrder(OrderRequest order) {
        Orders insertedOrder = mongoTemplate.save(new Orders(order.getOrderNumber(), order.getOrderedTime(), order.getProducts(), order.getSellerId()));

        // 만약 insertedOrder 에서 id가 null 이라면 문제가 생긴 것이다.
        if(insertedOrder.getId() == null) {
            throw new RuntimeException("MongoDB insertion 에러 발생!");
        }

        return OrderResponse.builder()
                .orderNumber(insertedOrder.getOrderNumber())
                .build();
    }

    public CancelAllOrderResponse cancelAllOrder(CancelAllOrderRequest cancelOrder) {
        Query removeQuery = new Query(Criteria.where("orderNumber").is(cancelOrder.getOrderNumber()));

        DeleteResult deleteResult = mongoTemplate.remove(removeQuery);

        if(deleteResult.getDeletedCount() != 1L) {
            throw new RuntimeException("MongoDB Delete 에러!");
        }

        return CancelAllOrderResponse.builder()
                .orderNumber(cancelOrder.getOrderNumber())
                .refund(true)
                .canceled(true)
                .build();
    }

    public CancelPartialOrderResponse cancelPartialOrder(CancelPartialOrderRequest cancelOrder) {
        Query findQuery = new Query(Criteria.where("orderNumber").is(cancelOrder.getOrderNumber()));
        Update updateQuery = new Update().pull("products", cancelOrder.getProductsToCancel());

        UpdateResult updateResult = mongoTemplate.updateFirst(findQuery, updateQuery, Orders.class);

        if(updateResult.getMatchedCount() != 1 || updateResult.getModifiedCount() != 1)
            throw new RuntimeException("부분 취소 에러!");

        Orders orders = mongoTemplate.findOne(findQuery, Orders.class);

        return CancelPartialOrderResponse.builder()
                .orderNumber(cancelOrder.getOrderNumber())
                .products(orders.getProducts())
                .refund(true)
                .canceled(true)
                .sellerId(cancelOrder.getSellerId())
                .build();
    }
}
