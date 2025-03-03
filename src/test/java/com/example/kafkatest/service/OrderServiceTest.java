package com.example.kafkatest.service;

import com.example.kafkatest.dto.request.CancelAllOrderRequest;
import com.example.kafkatest.dto.request.CancelPartialOrderRequest;
import com.example.kafkatest.dto.request.OrderRequest;
import com.example.kafkatest.dto.response.CancelAllOrderResponse;
import com.example.kafkatest.dto.response.CancelPartialOrderResponse;
import com.example.kafkatest.dto.response.OrderResponse;
import com.example.kafkatest.entity.document.Orders;
import com.example.kafkatest.entity.document.Products;
import com.example.kafkatest.entity.document.Sellers;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = OrderService.class)
class OrderServiceTest {
    // @SpringBootTest를 사용할 때는 @MockBean을 사용해야한다.
    @MockBean
    MongoTemplate mongoTemplate;

    @Autowired
    OrderService orderService;

    List<Products> products;

    static final String ORDER_NUMBER = "92838174";
    static final String SELLER_ID = "298371";
    static final String BUSINESS_NAME = "Raon Shop";
    static final String ACCOUNT_NUMBER = "938-28381";
    static final String ADDRESS = "Seoul, Republic Of Korea";
    static final String TELEPHONE = "02-838-3273";

    @BeforeEach
    void init() {
        products = new ArrayList<>();

        products.add(new Products("Taylor 314CE", 3450000L, 1L));

        products.add(new Products("G7th Capo", 50000L, 1L));

        products.add(new Products("Elixir Pospo Bronze", 28000L, 2L));

        products.add(new Products("Yu-gi-oh Quarter Century Edition",
                55000L, 5L));
    }

    @Test
    void 주문하기() {
        // GIVEN
        OrderRequest order = OrderRequest.builder()
                .orderedTime(Instant.now().atZone(ZoneId.of("Asia/Seoul")).toString())
                .products(products)
                .orderNumber(ORDER_NUMBER)
                .build();
        Orders savedOrders = new Orders(order.getOrderNumber(), order.getOrderedTime(),
                order.getProducts(), SELLER_ID);

        // WHEN
        when(mongoTemplate.save(any(Orders.class))).thenReturn(savedOrders);
        OrderResponse orderResponse = orderService.publishOrder(order);

        // THEN
        assertEquals(orderResponse.getOrderNumber(), ORDER_NUMBER);
    }

    @Test
    void 주문을_전체_취소하고_즉시_환불() {
        // GIVEN
        CancelAllOrderRequest orderCancel = CancelAllOrderRequest.builder()
                .orderNumber(ORDER_NUMBER)
                .instantRefund(true)
                .sellerId(SELLER_ID)
                .build();

        Query removeQuery = new Query(Criteria.where("orderNumber").is(ORDER_NUMBER));

        // WHEN
        when(mongoTemplate.findOne(removeQuery, Orders.class)).thenReturn(new Orders(ORDER_NUMBER,
                Instant.now().atZone(ZoneId.of("Asia/Seoul")).toString(), products, SELLER_ID));
        when(mongoTemplate.remove(removeQuery)).thenReturn(DeleteResult.acknowledged(1L));
        CancelAllOrderResponse canceledOrder = orderService.cancelAllOrder(orderCancel);

        // THEN
        assertEquals(canceledOrder.getOrderNumber(), ORDER_NUMBER);
        assertTrue(canceledOrder.isRefund());
        assertTrue(canceledOrder.isCanceled());
    }

    // 부분 환불 이런 것들은 PaymentService 에서 다룰 예정이다.
    @Test
    void 주문을_부분_취소하고_즉시_환불() {
        // Given
        List<Products> canceledProducts = new ArrayList<>();
        canceledProducts.add(new Products("Yu-gi-oh Quarter Century Edition",
                55000L, 5L));
        CancelPartialOrderRequest orderCancel = CancelPartialOrderRequest.builder()
                .orderNumber(ORDER_NUMBER)
                .productsToCancel(canceledProducts)
                .instantRefund(true)
                .sellerId(SELLER_ID)
                .build();
        // remove product where product name is canceled products.
        products.removeIf(products -> products.productName().equals(canceledProducts.get(0).productName()));
        Orders foundOrder = new Orders(ORDER_NUMBER,
                Instant.now().atZone(ZoneId.of("Asia/Seoul")).toString(),
                products, SELLER_ID);

        Query findQuery = new Query(Criteria.where("orderNumber").is(ORDER_NUMBER));
        Update updateQuery = new Update().pull("products", canceledProducts);

        // When
        when(mongoTemplate.updateFirst(findQuery, updateQuery, Orders.class))
                .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
        when(mongoTemplate.findOne(findQuery, Orders.class)).thenReturn(foundOrder);
        CancelPartialOrderResponse afterCanceled = orderService.cancelPartialOrder(orderCancel);

        // Then
        assertEquals(afterCanceled.getProducts().size(), 3);
        assertTrue(afterCanceled.isRefund());
        assertTrue(afterCanceled.isCanceled());
    }
}