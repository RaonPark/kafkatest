package com.example.kafkatest.entity.document;

import lombok.Data;
import org.apache.kafka.common.protocol.types.Field;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "orders")
public class Orders {
    @Id
    private ObjectId id = new ObjectId();

    private String orderNumber;
    private String orderedTime;
    private List<Products> products;
    private String sellerId;

    public Orders(
            final String orderNumber,
            final String orderedTime,
            final List<Products> products,
            final String sellerId
    ) {
        this.orderNumber = orderNumber;
        this.orderedTime = orderedTime;
        this.products = products;
        this.sellerId = sellerId;
    }
}
