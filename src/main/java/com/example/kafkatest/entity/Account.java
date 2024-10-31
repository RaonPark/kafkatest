package com.example.kafkatest.entity;

import com.example.kafkatest.support.BaseTimeEntity;
import com.example.kafkatest.support.LocalDateTimeFormatter;
import com.example.kafkatest.vo.AccountVo;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ACCOUNT")
@NoArgsConstructor
public class Account extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "account_id")
    private UUID id;

    @Column(nullable = false)
    private String accountNumber;


    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean isClosed;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal balance;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    private Account(AccountVo accountVo, Member member) {
        accountNumber = accountVo.getAccountNumber();
        password = accountVo.getPassword();
        isClosed = accountVo.getIsClosed();
        balance = accountVo.getBalance();
        this.member = member;
    }

    public AccountVo toAccountVo() {
        return AccountVo.builder()
                .accountNumber(accountNumber)
                .password(password)
                .isClosed(isClosed)
                .balance(balance)
                .openDate(LocalDateTimeFormatter.toYYYYMMddString(getCreatedTime()))
                .build();
    }
}
