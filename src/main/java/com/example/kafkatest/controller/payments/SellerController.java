package com.example.kafkatest.controller.payments;

import com.example.kafkatest.dto.request.payments.SellerRegisterRequest;
import com.example.kafkatest.dto.response.payments.SellerRegisterResponse;
import com.example.kafkatest.service.payments_service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;

    @PostMapping("/registerSeller")
    public ResponseEntity<SellerRegisterResponse> registerSeller(@RequestBody SellerRegisterRequest request) {
        SellerRegisterResponse response = sellerService.registerSeller(request);

        if(!response.registered())
            return ResponseEntity.internalServerError().body(response);
        return ResponseEntity.ok(response);
    }
}
