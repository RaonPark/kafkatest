package com.example.kafkatest.service.payments_service;

import com.example.kafkatest.dto.request.payments.CancelAllOrderRequest;
import com.example.kafkatest.dto.request.payments.CancelPartialOrderRequest;
import com.example.kafkatest.dto.request.payments.OrderRequest;
import com.example.kafkatest.dto.request.payments.PaymentRequest;
import com.example.kafkatest.dto.response.payments.CancelAllOrderResponse;
import com.example.kafkatest.dto.response.payments.CancelPartialOrderResponse;
import com.example.kafkatest.dto.response.payments.OrderResponse;
import com.example.kafkatest.dto.response.payments.PaymentResponse;
import com.example.kafkatest.entity.payments.document.*;
import com.example.kafkatest.service.RedisService;
import com.example.kafkatest.support.enums.ProcessStage;
import com.example.kafkatest.support.enums.ProcessType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.raonpark.OrderPaymentOutboxAvro;
import com.raonpark.PaymentData;
import com.raonpark.RevenueData;
import com.raonpark.avro.PaymentOutboxMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, PaymentData> paymentDataKafkaTemplate;
    private final KafkaTemplate<String, RevenueData> revenueDataKafkaTemplate;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    public OrderResponse publishOrder(OrderRequest order) {
        String orderNumber = generateOrderNumber(order);
        String orderedTime = Instant.now().atZone(ZoneId.of("Asia/Seoul")).toString();
        Orders orders = new Orders(orderNumber, orderedTime, order.products(), order.sellerId());
        Orders insertedOrder = mongoTemplate.save(orders);

        // 만약 insertedOrder 에서 id가 null 이라면 문제가 생긴 것이다.
        if(insertedOrder.getId() == null) {
            throw new RuntimeException("MongoDB insertion 에러 발생!");
        }

        Query findSellerQuery = new Query(Criteria.where("sellerId").is(order.sellerId()));
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

    public OrderResponse publishOrderWithOutbox(OrderRequest order, PaymentRequest payment, long aggId) {
        String orderNumber = generateOrderNumber(order);
        String orderedTime = Instant.now().atZone(ZoneId.of("Asia/Seoul")).toString();
        Orders orders = new Orders(orderNumber, orderedTime, order.products(), order.sellerId());

        /**
         * 먼저 orders document 를 저장하고
         * 그 다음에 outbox 에 저장한다.
         * CompletableFuture 를 사용하여 동기를 보장한다.
         */
        CompletableFuture<Orders> savedOrderFuture = CompletableFuture.supplyAsync(() -> mongoTemplate.save(orders))
                .thenApply(savedOrder -> {
                    String payload = paymentDataToString(payment);


                    OrderPaymentOutbox orderPaymentOutbox = OrderPaymentOutbox.builder()
                            .aggId(String.valueOf(aggId))
                            .payload(payload)
                            .processType(ProcessType.ORDER)
                            .build();
                    mongoTemplate.save(orderPaymentOutbox);


                    return savedOrder;
                });

        Query findSellerQuery = new Query(Criteria.where("sellerId").is(order.sellerId()));
        Sellers seller = Optional.ofNullable(mongoTemplate.findOne(findSellerQuery, Sellers.class))
                .orElseThrow(() -> new RuntimeException("해당 점포를 찾을 수 없습니다!"));

        ReceiptSellerInfo sellerInfo = ReceiptSellerInfo.builder()
            .address(seller.getAddress())
            .businessName(seller.getBusinessName())
            .telephone(seller.getTelephone())
            .build();

        Orders insertedOrder = savedOrderFuture.join();

        log.info("in orderService = {}", orderNumber);

        return OrderResponse.builder()
                .orderNumber(insertedOrder.getOrderNumber())
                .sellerInfo(sellerInfo)
                .orderedTime(insertedOrder.getOrderedTime())
                .products(insertedOrder.getProducts())
                .build();
    }

    public boolean waitUntilPaymentFinished(long aggId, int retries) {
        if(retries == -1)
            return false;
        log.info("Process Stage를 기다리는 중 : id = {} retries = {}", aggId, retries);
        ProcessType processType = Optional.ofNullable(
                redisService.findHash("order", String.valueOf(aggId), ProcessType.class))
                .orElse(ProcessType.NOT_PROCESSED);

        if(processType.equals(ProcessType.PAYMENT) || processType.equals(ProcessType.TOTAL_REVENUE)) {
            return true;
        }

        try {
            Thread.sleep(1000L);
        } catch(InterruptedException e) {
            throw new RuntimeException("Thread Error");
        }

        return waitUntilPaymentFinished(aggId, retries - 1);
    }

    @KafkaListener(topics = {"order-payment-outbox.topic"}, containerFactory = "orderPaymentOutboxConcurrentKafkaListenerContainerFactory")
    public void consumeOrder(ConsumerRecord<String, OrderPaymentOutboxAvro> record) {
        OrderPaymentOutboxAvro outbox = record.value();
        ProcessType processType = ProcessType.toStage(outbox.getProcessStage().toString());
        if(processType.equals(ProcessType.PAYMENT) || processType.equals(ProcessType.TOTAL_REVENUE)) {
            log.info("process 가 완료되어 레디스에 완료를 함 = {}", outbox);
            redisService.saveHash("order", outbox.getAggId().toString(), processType);
        }
    }

    private String paymentDataToString(PaymentRequest payment) {
        try {
            return objectMapper.writeValueAsString(payment);
        } catch(JsonProcessingException e) {
            throw new RuntimeException("Json Processing ERROR!");
        }
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
                .setSellerId(order.sellerId())
                .build();

        log.info("send revenueData from orderService = {}", revenueData);

        revenueDataKafkaTemplate.send("revenueData", orderNumber, revenueData);
    }

    private long computeAmount(OrderRequest order) {
        return order.products().stream().map(products -> products.price() * products.quantity())
                .reduce(Long::sum)
                .orElse(0L);
    }

    public OrderResponse createNewOrder(OrderRequest orderRequest, PaymentRequest paymentRequest, long aggId) {
        String orderNumber = generateOrderNumber(orderRequest);
        String orderedTime = Instant.now().atZone(ZoneId.of("Asia/Seoul")).toString();
        Orders orders = new Orders(orderNumber, orderedTime, orderRequest.products(), orderRequest.sellerId());

        CompletableFuture<Orders> createdOrderFuture = CompletableFuture.supplyAsync(() -> mongoTemplate.save(orders))
                .thenApply(savedOrder -> {
                    String paymentPayload = paymentDataToString(paymentRequest);
                    OrderOutbox orderOutbox = OrderOutbox.builder()
                            .aggId(String.valueOf(aggId))
                            .processStage(ProcessStage.PROCESSED)
                            .payload(paymentPayload)
                            .build();

                    mongoTemplate.save(orderOutbox);

                    return savedOrder;
                });

        Query findSellerQuery = new Query(Criteria.where("sellerId").is(orderRequest.sellerId()));
        Sellers seller = Optional.ofNullable(mongoTemplate.findOne(findSellerQuery, Sellers.class))
                .orElseThrow(() -> new RuntimeException(orderRequest.sellerId() + "에 해당하는 판매자가 없습니다."));

        Orders createdOrder = createdOrderFuture.join();

        ReceiptSellerInfo sellerInfo = ReceiptSellerInfo.builder()
                .address(seller.getAddress())
                .telephone(seller.getTelephone())
                .businessName(seller.getBusinessName())
                .build();

        return OrderResponse.builder()
                .orderedTime(createdOrder.getOrderedTime())
                .orderNumber(createdOrder.getOrderNumber())
                .products(createdOrder.getProducts())
                .sellerInfo(sellerInfo)
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

    private String generateOrderNumber(OrderRequest order) {
        String time = Long.toHexString(Instant.now().toEpochMilli());
        String orderProductsSize = Long.toHexString(order.products().size());

        return time + order.sellerId().substring(0, 4) + orderProductsSize;
    }
}
