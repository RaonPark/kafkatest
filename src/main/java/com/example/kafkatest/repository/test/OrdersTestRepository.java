package com.example.kafkatest.repository.test;

import com.example.kafkatest.entity.document.Orders;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OrdersTestRepository extends MongoRepository<Orders, String> {
    Optional<Orders> findByOrderNumber(final String orderNumber);
}
