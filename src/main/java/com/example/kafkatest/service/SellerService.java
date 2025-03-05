package com.example.kafkatest.service;

import com.example.kafkatest.dto.request.SellerRegisterRequest;
import com.example.kafkatest.dto.response.SellerRegisterResponse;
import com.example.kafkatest.entity.document.Sellers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {
    private final MongoTemplate mongoTemplate;

    public SellerRegisterResponse registerSeller(SellerRegisterRequest seller) {
        Sellers registerSeller = Sellers.builder()
                .sellerId(generateSellerId(seller.businessName()))
                .telephone(seller.telephone())
                .address(seller.address())
                .accountNumber(seller.accountNumber())
                .businessName(seller.businessName())
                .build();

        Sellers savedSellers = Optional.of(mongoTemplate.save(registerSeller))
                .orElseThrow(() -> new RuntimeException("MongoDB 에러!"));

        if(savedSellers.getId() == null)
            throw new RuntimeException("MongoDB 에러!");

        return SellerRegisterResponse.builder()
                .registered(true)
                .sellerId(savedSellers.getSellerId())
                .build();
    }

    private String generateSellerId(String businessName) {
        String timeToHex = Long.toHexString(Instant.now().toEpochMilli());
        String businessNameToHex = String.format("%040x", new BigInteger(1, businessName.getBytes(StandardCharsets.UTF_8)));

        return timeToHex + businessNameToHex;
    }
}
