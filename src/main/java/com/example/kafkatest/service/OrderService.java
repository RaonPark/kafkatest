package com.example.kafkatest.service;

import com.example.kafkatest.dto.request.CancelAllOrderRequest;
import com.example.kafkatest.dto.request.CancelPartialOrderRequest;
import com.example.kafkatest.dto.request.OrderRequest;
import com.example.kafkatest.dto.response.CancelAllOrderResponse;
import com.example.kafkatest.dto.response.CancelPartialOrderResponse;
import com.example.kafkatest.dto.response.OrderResponse;
import com.example.kafkatest.entity.document.Orders;
import com.example.kafkatest.entity.document.ReceiptSellerInfo;
import com.example.kafkatest.entity.document.Sellers;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

        Query findSellerQuery = new Query(Criteria.where("sellerId").is(order.getSellerId()));
        Sellers seller = Optional.ofNullable(mongoTemplate.findOne(findSellerQuery, Sellers.class))
                .orElseThrow(() -> new RuntimeException("해당 점포를 찾을 수 없습니다!"));

        ReceiptSellerInfo sellerInfo = ReceiptSellerInfo.builder()
                .address(seller.getAddress())
                .businessName(seller.getBusinessName())
                .telephone(seller.getTelephone())
                .build();

        return OrderResponse.builder()
                .orderNumber(insertedOrder.getOrderNumber())
                .sellerInfo(sellerInfo)
                .orderedTime(insertedOrder.getOrderedTime())
                .products(insertedOrder.getProducts())
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

        Orders orders = Optional.ofNullable(mongoTemplate.findOne(findQuery, Orders.class))
                .orElseThrow(() -> new RuntimeException("Mongo DB 에러!"));

        return CancelPartialOrderResponse.builder()
                .orderNumber(cancelOrder.getOrderNumber())
                .products(orders.getProducts())
                .refund(true)
                .canceled(true)
                .sellerId(cancelOrder.getSellerId())
                .build();
    }
}
