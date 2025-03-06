package com.example.kafkatest.service;

import com.example.kafkatest.dto.request.*;
import com.example.kafkatest.dto.response.CancelAllOrderResponse;
import com.example.kafkatest.dto.response.CancelPartialOrderResponse;
import com.example.kafkatest.dto.response.OrderResponse;
import com.example.kafkatest.entity.document.Orders;
import com.example.kafkatest.entity.document.Products;
import com.example.kafkatest.entity.document.ReceiptSellerInfo;
import com.example.kafkatest.entity.document.Sellers;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.raonpark.PaymentData;
import com.raonpark.RevenueData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, PaymentData> paymentDataKafkaTemplate;
    private final KafkaTemplate<String, RevenueData> revenueDataKafkaTemplate;
    private final RedisService redisService;

    public OrderResponse publishOrder(OrderRequest order) {
        String orderNumber = generateOrderNumber(order);
        String orderedTime = Instant.now().atZone(ZoneId.of("Asia/Seoul")).toString();
        Orders orders = new Orders(orderNumber, orderedTime, order.getProducts(), order.getSellerId());
        Orders insertedOrder = mongoTemplate.save(orders);

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

        log.info("in orderService = {}", insertedOrder.getOrderNumber());

        redisService.incrDelta(orderNumber, 100L);

        return OrderResponse.builder()
                .orderNumber(insertedOrder.getOrderNumber())
                .sellerInfo(sellerInfo)
                .orderedTime(insertedOrder.getOrderedTime())
                .products(insertedOrder.getProducts())
                .build();
    }

    public void sendPaymentData(String orderNumber, OrderRequest order, PaymentRequest payment) {
        long amount = computeAmount(order);

        PaymentData paymentData = PaymentData.newBuilder()
                .setOrderNumber(orderNumber)
                .setAmount(amount)
                .setPaymentType(payment.paymentType().toString())
                .setCardCompany(payment.cardCompany())
                .setCardCvc(payment.cardCvc())
                .setCardNumber(payment.cardNumber())
                .build();

        log.info("send paymentData from orderService = {}", paymentData);

        paymentDataKafkaTemplate.send("paymentData", orderNumber, paymentData);
    }

    public void sendRevenueData(String orderNumber, OrderRequest order) {
        long revenue = computeAmount(order);
        RevenueData revenueData = RevenueData.newBuilder()
                .setOrderNumber(orderNumber)
                .setRevenue(revenue)
                .setSellerId(order.getSellerId())
                .build();

        log.info("send revenueData from orderService = {}", revenueData);

        revenueDataKafkaTemplate.send("revenueData", orderNumber, revenueData);
    }

    private long computeAmount(OrderRequest order) {
        return order.getProducts().stream().map(products -> products.price() * products.quantity())
                .reduce(Long::sum)
                .orElse(0L);
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

    private String generateOrderNumber(OrderRequest order) {
        String time = Long.toHexString(Instant.now().toEpochMilli());
        String orderProductsSize = Long.toHexString(order.getProducts().size());

        return time + order.getSellerId().substring(0, 4) + orderProductsSize;
    }
}
