package com.example.kafkatest.controller;

import com.example.kafkatest.dto.request.PublishOrderRequest;
import com.example.kafkatest.dto.response.OrderResponse;
import com.example.kafkatest.service.OrderService;
import com.example.kafkatest.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final RedisService redisService;

    @PostMapping("/publishOrder")
    public ResponseEntity<OrderResponse> publishOrder(@RequestBody PublishOrderRequest request) {
        // Payment, Order 둘만 성공하면 된다.
        // 문제는 이걸 CompletableFuture 를 사용하더라도 문제가 생긴다. 그래서 우리는 결국 이걸 어떻게 해결할지가 문제다.
        // transaction counts 가 2가 되는지 확인하는 것은 결국에 race condition 이다.
        // 하지만 이건 Redis 의 INCR 를 이용하면 된다. 그건 atomic 하다.
        // CompletableFuture 를 사용해도 결국 redis 에 transaction 이 쌓이는 것을 기다려야한다.
        // 어찌되었든 주문보다 결제가 중요하다고 생각하기 때문에 먼저 결제를 하고 나중에 order 를 하는 것은 어떨까?
        // 사실 주문 정보를 저장하는 것은 당연히 온라인 샵에서는 중요할 것이다.
        // 하지만 지금 생각하는 것은 오프라인 샵이므로 결제 정보를 먼저 저장하는 것이 중요하다고 생각한다.

        // 일단 우선은 order 를 먼저 하여 구현을 해보자.
        CompletableFuture<OrderResponse> orderFuture = CompletableFuture.supplyAsync(() ->
                orderService.publishOrder(request.orderRequest()))
                .thenApply(orderResponse -> {
                    orderService.sendPaymentData(orderResponse.orderNumber(), request.orderRequest(), request.paymentRequest());
                    orderService.sendRevenueData(orderResponse.orderNumber(), request.orderRequest());
                    return orderResponse;
                })
                .thenApply(orderResponse -> {
                    log.info("send data to other service. payment = {} order = {}", request.paymentRequest(), request.orderRequest());
                    log.info("get response of order service = {}", orderResponse);
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    long transaction = redisService.findAsLong(orderResponse.orderNumber());
                    log.info("get response of transaction = {}", transaction);
                    if(transaction >= 110)
                        return orderResponse;
                    else
                        return OrderResponse.builder()
                                .orderNumber("FAILED")
                                .build();
                });

        OrderResponse response = orderFuture.join();

        if(response.orderNumber().equals("FAILED"))
            return ResponseEntity.internalServerError().body(response);

        return ResponseEntity.ok(response);
    }
}
