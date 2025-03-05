package com.example.kafkatest.service;

import com.example.kafkatest.dto.request.SellerRegisterRequest;
import com.example.kafkatest.dto.response.SellerRegisterResponse;
import com.example.kafkatest.entity.document.Sellers;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SellerService.class)
public class SellerServiceTest {
    @MockBean
    MongoTemplate mongoTemplate;

    @Autowired
    SellerService sellerService;

    static final String BUSINESS_NAME = "HELLO WORLD!";
    static final String ADDRESS = "서울시 서초구";
    static final String ACCOUNT_NUMBER = "0293-283303-23-12";
    static final String TELEPHONE = "010-2939-1929";
    public static final String SELLER_ID = Long.toHexString(100)
            + String.format("%040x", new BigInteger(1, BUSINESS_NAME.getBytes(StandardCharsets.UTF_8)));

    @Test
    void 판매처_등록하기() {
        // given
        SellerRegisterRequest seller = SellerRegisterRequest.builder()
                .businessName(BUSINESS_NAME)
                .address(ADDRESS)
                .accountNumber(ACCOUNT_NUMBER)
                .telephone(TELEPHONE)
                .build();

        Sellers savedSeller = Sellers.builder()
                .sellerId(SELLER_ID)
                .accountNumber(seller.accountNumber())
                .businessName(seller.businessName())
                .address(seller.address())
                .telephone(seller.telephone())
                .build();

        // when
        when(mongoTemplate.save(any(Sellers.class))).thenReturn(savedSeller);
        SellerRegisterResponse response = sellerService.registerSeller(seller);

        // then
        assertEquals(savedSeller.getSellerId(), response.sellerId());
        assertTrue(response.registered());
    }
}
