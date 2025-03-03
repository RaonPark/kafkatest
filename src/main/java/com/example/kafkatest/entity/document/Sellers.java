package com.example.kafkatest.entity.document;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("sellers")
public class Sellers {
    @Id
    private ObjectId id = new ObjectId();
    private String sellerId;
    private String businessName;
    private String accountNumber;
    private String address;
    private String telephone;

    public Sellers(
            final String sellerId,
            final String businessName,
            final String accountNumber,
            final String address,
            final String telephone
    ) {
        this.sellerId = sellerId;
        this.businessName = businessName;
        this.accountNumber = accountNumber;
        this.address = address;
        this.telephone = telephone;
    }
}
