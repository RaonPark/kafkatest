package com.example.kafkatest.controller;

import com.example.kafkatest.dto.MakeAccountDto;
import com.example.kafkatest.dto.request.MakeChatRoomRequest;
import com.example.kafkatest.dto.request.PutMoneyRequest;
import com.example.kafkatest.dto.request.SendChatMessageRequest;
import com.example.kafkatest.entity.Member;
import com.example.kafkatest.repository.AccountRepository;
import com.example.kafkatest.repository.MemberRepository;
import com.example.kafkatest.service.AccountService;
import com.example.kafkatest.service.ChatService;
import com.example.kafkatest.service.RedisService;
import com.example.kafkatest.support.ChatMessageType;
import com.example.kafkatest.vo.MemberVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestKafkaRepoController {
    private final MemberRepository memberRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final RedisService redisService;
    private final ChatService chatService;

    @GetMapping("/integrateTestWithJMeter")
    public String integrate() {
        // given
        String userId = UUID.randomUUID().toString();

        // REGISTER
        MemberVo memberVo = MemberVo.builder()
                .username("test")
                .userId(userId)
                .email("test@naver.com")
                .address("서울특별시")
                .password("1234")
                .build();

        Member member = Member.builder()
                .memberVo(memberVo)
                .build();

        memberRepository.save(member);

        // CREATE ACCOUNT
        String accountNumber = accountService.makeAccount(MakeAccountDto.MakeAccountRequest.builder()
                        .userId(userId)
                        .password("1234")
                .build()).getAccountNumber();

        // SAVING
        accountService.putMoneyAndProduceMessage(PutMoneyRequest.builder()
                        .accountNumber(accountNumber)
                        .money(BigDecimal.valueOf(100000))
                        .nickname("hello")
                        .userId(userId)
                .build());

        return redisTemplate.opsForValue().get("balance");
    }

    @DeleteMapping("/cleanup")
    public void cleanup() {
        accountRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @GetMapping("/chatIntegrate")
    public long chatTest() {
        long start = System.currentTimeMillis();
        ArrayList<String> userIdList = new ArrayList<>();
        // given
        for(int i=0; i<10; i++) {
            String userId = UUID.randomUUID().toString();
            userIdList.add(userId);

            // REGISTER
            MemberVo memberVo = MemberVo.builder()
                    .username("test")
                    .userId(userId)
                    .email("test@naver.com")
                    .address("서울특별시")
                    .password("1234")
                    .build();

            Member member = Member.builder()
                    .memberVo(memberVo)
                    .build();

            memberRepository.save(member);
        }

        // login
        log.info("logged in : {}\n", userIdList.get(0));
        redisService.save("currentUser", userIdList.get(0));

        // make chatting room
        MakeChatRoomRequest request = MakeChatRoomRequest.builder()
                .roomName("hello").numberOfMember(10).userId(userIdList).build();
        long chatRoomId = chatService.createChatRoom(request);
        log.info("{}이 만들어졌습니다!\n", chatRoomId);

        // send 10000 chats
        for(int i=0; i<10000; i++) {
            SendChatMessageRequest sendChat = SendChatMessageRequest.builder()
                    .chatMessageType(ChatMessageType.TEXT).chatRoomId(chatRoomId)
                    .message("get message from " + userIdList.get(0) + " " + i)
                    .image("")
                    .emoticon("")
                    .build();
            chatService.produceChat(sendChat);
        }

        long end = System.currentTimeMillis();
        return end - start;
    }

    @GetMapping("/chatWithoutKafka")
    public long chatIntegrateWOKafka() {
        long start = System.currentTimeMillis();
        ArrayList<String> userIdList = new ArrayList<>();
        // given
        for(int i=0; i<10; i++) {
            String userId = UUID.randomUUID().toString();
            userIdList.add(userId);

            // REGISTER
            MemberVo memberVo = MemberVo.builder()
                    .username("test")
                    .userId(userId)
                    .email("test@naver.com")
                    .address("서울특별시")
                    .password("1234")
                    .build();

            Member member = Member.builder()
                    .memberVo(memberVo)
                    .build();

            memberRepository.save(member);
        }

        // login
        log.info("logged in : {}\n", userIdList.get(0));
        redisService.save("currentUser", userIdList.get(0));

        // make chatting room
        MakeChatRoomRequest request = MakeChatRoomRequest.builder()
                .roomName("hello").numberOfMember(10).userId(userIdList).build();
        long chatRoomId = chatService.createChatRoom(request);
        log.info("{}이 만들어졌습니다!\n", chatRoomId);

        // send chats
        for(int i=0; i<10000; i++) {
            SendChatMessageRequest sendChat = SendChatMessageRequest.builder()
                    .chatMessageType(ChatMessageType.TEXT).chatRoomId(chatRoomId)
                    .message("get message from " + userIdList.get(0) + " " + i)
                    .image("")
                    .emoticon("")
                    .build();

            chatService.produceChatWOKafka(sendChat);
        }

        long end = System.currentTimeMillis();

        return end - start;
    }
}
