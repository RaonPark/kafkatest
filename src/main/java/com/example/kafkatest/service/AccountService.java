package com.example.kafkatest.service;

import com.example.kafkatest.dto.MakeAccountDto;
import com.example.kafkatest.dto.request.PutMoneyRequest;
import com.example.kafkatest.entity.Account;
import com.example.kafkatest.entity.Member;
import com.example.kafkatest.repository.AccountRepository;
import com.example.kafkatest.repository.MemberRepository;
import com.example.kafkatest.support.Constants;
import com.example.kafkatest.support.RandomNumberGenerator;
import com.example.kafkatest.vo.AccountVo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final KafkaTemplate<String, PutMoneyRequest> jsonKafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public MakeAccountDto.MakeAccountResponse makeAccount(MakeAccountDto.MakeAccountRequest request) {
        Member member = Optional.of(memberRepository.findByUserId(request.getUserId()))
                .orElseThrow(NoSuchElementException::new);

        AccountVo accountVo = request.toAccountVo();
        String accountNumber = makeAccountNumber(request.getUserId());
        accountVo.setIsClosed(false);
        accountVo.setAccountNumber(accountNumber);
        accountVo.setBalance(BigDecimal.ZERO);

        Account account = Account.builder()
                .accountVo(accountVo)
                .member(member)
                .build();
        accountRepository.save(account);

        return MakeAccountDto.MakeAccountResponse.builder()
                .accountNumber(accountNumber)
                .build();
    }

    private String makeAccountNumber(String userId) {
        String uniqueNumber = Optional.of(memberRepository.findUniqueNumberByUserId(userId))
                .orElseThrow(NoSuchElementException::new);

        String randomNumber = RandomNumberGenerator.generateRandom6Numbers();

        return new StringBuilder()
                .append(Constants.BANK_UNIQUE_NUMBER)
                .append("-")
                .append(uniqueNumber)
                .append("-")
                .append(randomNumber).toString();
    }

    public void putMoneyAndProduceMessage(PutMoneyRequest request) {
        jsonKafkaTemplate.send("PutMoney", request).toCompletableFuture()
                .whenComplete((sendResult, throwable) -> {
                    log.info("파티션 = {}, 오프셋 = {}\n", sendResult.getRecordMetadata().partition(),
                            sendResult.getRecordMetadata().offset());
                });
    }

    // DB에 저장해보자...
    @Transactional
    @KafkaListener(groupId = "moneyToDB", topics = "PutMoney", containerFactory = "kafkaListenerForSavingBalance")
    public void putMoneyInAccountDB(ConsumerRecord<String, PutMoneyRequest> consumerRecord) {
        PutMoneyRequest request = consumerRecord.value();
        int updateResult = accountRepository.updateBalanceByAccountNumber(request.getAccountNumber(), request.getMoney());
        if(updateResult != 1)
            throw new IllegalStateException("같은 번호의 통장이 없거나 2개 이상입니다!");

        BigDecimal balance =
                Optional.of(accountRepository.findBalanceByAccountNumber(request.getAccountNumber()))
                        .orElseThrow(NoSuchElementException::new);

        log.info("토픽 = {}, 통장 번호 = {}, 잔액 = {}\n", "PutMoney", request.getAccountNumber(), balance);
    }

    @KafkaListener(groupId = "moneyToDisplay", topics = "PutMoney", containerFactory = "kafkaListenerForBalanceDisplay")
    public void putMoneyAndDisplay(ConsumerRecord<String, PutMoneyRequest> consumerRecord) {
        PutMoneyRequest request = consumerRecord.value();
        BigDecimal balance = Optional.of(accountRepository.findBalanceByAccountNumber(request.getAccountNumber()))
                .orElseThrow(NoSuchElementException::new);

        log.info("토픽 = {}, 통장 번호 = {}, 잔액 = {}\n", "PutMoney", request.getAccountNumber(), balance.add(request.getMoney()));
        redisTemplate.opsForValue().append("balance", balance.add(request.getMoney()).toString());
    }
}
