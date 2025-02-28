package com.example.kafkatest.mongotest;

import com.example.kafkatest.entity.document.Orders;
import com.example.kafkatest.entity.document.Products;
import com.example.kafkatest.repository.test.OrdersTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MongoController {
    private final OrdersTestRepository ordersTestRepository;
    @GetMapping("/testOrderInsert")
    public void insertOrder() {
        Products product = new Products("테일러 314CE", 3450000, 1);
        List<Products> products = new ArrayList<>();
        products.add(product);
        String orderedTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString();
        ordersTestRepository.insert(new Orders(
                "30482",
                orderedTime,
                products,
                "398174"
        ));
    }

    @GetMapping("/testOrderFind")
    public void findOrder() {
        Orders order = ordersTestRepository.findByOrderNumber("30482")
                .orElseThrow(RuntimeException::new);
        log.info("""
                        there's new order!
                         orderNumber = {}, orderedTime = {},
                        productName = {}, price = {}, quantity = {},\s
                        seller = {}""", order.getOrderNumber(), order.getOrderedTime(),
                order.getProducts().get(0).productName(), order.getProducts().get(0).price(),
                order.getProducts().get(0).quantity(), order.getSellerId());
    }
}
