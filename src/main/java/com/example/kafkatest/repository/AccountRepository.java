package com.example.kafkatest.repository;

import com.example.kafkatest.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    @Modifying
    @Query("update Account set balance = balance + :money where accountNumber = :accountNumber")
    int updateBalanceByAccountNumber(String accountNumber, BigDecimal money);

    @Query("select balance from Account where accountNumber = :accountNumber")
    BigDecimal findBalanceByAccountNumber(String accountNumber);
}
