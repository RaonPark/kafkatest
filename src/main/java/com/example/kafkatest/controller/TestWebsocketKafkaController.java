package com.example.kafkatest.controller;

import com.example.kafkatest.dto.ChatMessageKafkaDTO;
import com.example.kafkatest.dto.request.MakeChatRoomRequest;
import com.example.kafkatest.dto.request.SendChatMessageRequest;
import com.example.kafkatest.entity.ChatMessage;
import com.example.kafkatest.entity.Chatroom;
import com.example.kafkatest.entity.Member;
import com.example.kafkatest.repository.ChatMessageRepository;
import com.example.kafkatest.repository.ChatroomRepository;
import com.example.kafkatest.repository.MemberRepository;
import com.example.kafkatest.service.ChatService;
import com.example.kafkatest.service.RedisService;
import com.example.kafkatest.support.ChatMessageType;
import com.example.kafkatest.vo.MemberVo;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TestWebsocketKafkaController {
    private final ChatService chatService;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatroomRepository chatroomRepository;
    private final RedisService redisService;
    private final MemberRepository memberRepository;
    private final KafkaTemplate<String, ChatMessageKafkaDTO> kafkaTemplate;

    @Transactional
    @GetMapping("/preWebsocket")
    public String preprocess() {
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

        return String.valueOf(chatRoomId);
    }

    @Transactional
    @MessageMapping("/chatWebsocket")
    @SendTo("/topic/chat")
    public ChatMessage chatWithWebsocket(String infos) {
        return chatService.produceChatWithWebsocket(infos);
    }

    @Transactional
    @GetMapping("/sendChat")
    public void sendChat(@RequestParam("infos") String infos, @RequestParam("order") int order) {
        infos = infos.replaceAll("\"", "");
        String[] info = infos.split("\\$");
        log.info("here's infos : {}, {}", info[0], info[1]);
        String userId = info[0];
        long chatRoomId = Long.parseLong(info[1]);
        redisService.save("kafka", new Date());

        SendChatMessageRequest sendChat = SendChatMessageRequest.builder()
                .chatMessageType(ChatMessageType.TEXT).chatRoomId(chatRoomId)
                .message("get message from " + userId + " order " + order)
                .image("")
                .emoticon("")
                .build();
        chatService.produceChat(sendChat);
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Setter
    public static class ChatResponseForTest {
        private String message;
        private int order;
    }

    @Transactional
    @MessageMapping("/withMillionMessages")
    @SendTo("/topic/messages")
    public ChatResponseForTest millionMessages(SendChatMessageRequest request) {
        log.info("{} message from websocket", request.getOrder());
        Chatroom chatroom = chatroomRepository.findById(request.getChatRoomId()).orElseThrow(RuntimeException::new);
        ChatMessage chatMessage = ChatMessage.builder().sendChatMessageRequest(request).chatRoom(chatroom).build();
        produceMessages(request);
        return ChatResponseForTest.builder().message(chatMessage.getMessage()).order(request.getOrder()).build();
    }

    public void produceMessages(SendChatMessageRequest request) {
        ChatMessageKafkaDTO dto = ChatMessageKafkaDTO.builder().message(request.getMessage()).order(request.getOrder()).build();
        kafkaTemplate.send("kvsw", dto);
    }

    @Transactional
    @KafkaListener(topics = "kvsw", groupId = "kvsw", containerFactory = "kafkaListenerContainerFactoryForKVSW")
    public void chatMessageListener(ConsumerRecord<String, ChatMessageKafkaDTO> record) {
        ChatMessageKafkaDTO chatMessage = record.value();
        log.info("{} message from kafka.", chatMessage.getMessage());
    }
}
