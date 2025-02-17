package com.example.kafkatest.service;

import com.example.kafkatest.dto.request.MakeChatRoomRequest;
import com.example.kafkatest.dto.request.SendChatMessageRequest;
import com.example.kafkatest.entity.ChatMessage;
import com.example.kafkatest.entity.Chatroom;
import com.example.kafkatest.entity.ChatroomMember;
import com.example.kafkatest.entity.Member;
import com.example.kafkatest.repository.*;
import com.example.kafkatest.support.ChatMessageType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatroomRepository chatroomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AnnouncementRepository announcementRepository;
    private final MemberRepository memberRepository;
    private final ChatroomMemberRepository chatRoomMemberRepository;
    private final RedisService redisService;
    private final KafkaTemplate<String, ChatMessage> chatMessageKafkaTemplate;

    @Transactional
    public long createChatRoom(MakeChatRoomRequest request) {
        Chatroom chatRoom = Chatroom.builder()
                .makeChatRoomRequest(request)
                .build();

        long id = chatroomRepository.save(chatRoom).getId();

        Chatroom savedChatroom = chatroomRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        List<Member> members = new ArrayList<>();
        for(int i=0; i<request.getNumberOfMember(); i++) {
            Member member = memberRepository.findByUserId(request.getUserId().get(i));
            members.add(member);
        }

        addMembersToChatRoom(savedChatroom, members);

        return id;
    }

    private void addMembersToChatRoom(Chatroom chatRoom, List<Member> members) {
        for (Member member : members) {
            ChatroomMember chatRoomMember = ChatroomMember.builder()
                    .chatRoom(chatRoom).member(member).role("USER").build();
            chatRoomMemberRepository.save(chatRoomMember);
        }
    }

    @Transactional
    @KafkaListener(groupId = "chat", topics = "chat", containerFactory = "kafkaListenerContainerFactoryForChat")
    public void sendChat(ConsumerRecord<String, ChatMessage> consumerRecord) {
        ChatMessage chatMessage = consumerRecord.value();

        String userId = redisService.find("currentUser", UUID.class).toString();
        List<Member> members = chatRoomMemberRepository.findMemberByChatRoomId(chatMessage.getChatRoom().getId());
        boolean isChatRoomMember = members.stream().anyMatch(member ->
            member.getUserId().equals(userId)
        );
        if(isChatRoomMember) {
            log.info("here's message {} first message produced in: {}\n", chatMessage.getMessage(),
                    redisService.find("kafka", Date.class));
        }
    }

    @Transactional
    public void produceChat(SendChatMessageRequest request) {
        Chatroom chatRoom = chatroomRepository.findById(request.getChatRoomId()).orElseThrow(IllegalArgumentException::new);
        ChatMessage chatMessage = ChatMessage.builder().sendChatMessageRequest(request).chatRoom(chatRoom).build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);
        chatMessageKafkaTemplate.send("chat", chatMessage);
    }

    @Transactional
    public void produceChatWOKafka(SendChatMessageRequest request) {
        Chatroom chatRoom = chatroomRepository.findById(request.getChatRoomId()).orElseThrow(IllegalArgumentException::new);
        ChatMessage chatMessage = ChatMessage.builder().sendChatMessageRequest(request).chatRoom(chatRoom).build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);

        String message = getMessage(chatRoom);
        log.info("get message without kafka. {}\n", message);
    }

    private String getMessage(Chatroom chatRoom) {
        List<ChatMessage> chats = chatMessageRepository.findChatMessageByChatroomId(chatRoom.getId());

        return chats.get(chats.size() - 1).getMessage();
    }

    @Transactional
    public ChatMessage produceChatWithWebsocket(String infos) {
        infos = infos.replaceAll("\"", "");
        String[] info = infos.split("\\$");
        log.info("here's infos : {}, {}, {}", info[0], info[1], info[2]);
        String userId = info[0];
        long chatRoomId = Long.parseLong(info[1]);

        SendChatMessageRequest sendChat = SendChatMessageRequest.builder()
                .chatMessageType(ChatMessageType.TEXT).chatRoomId(chatRoomId)
                .message("get message from " + userId)
                .image("")
                .emoticon("")
                .build();

        Chatroom chatRoom = chatroomRepository.findById(sendChat.getChatRoomId()).orElseThrow(IllegalArgumentException::new);
        long id = chatRoom.getId();
        ChatMessage chatMessage = ChatMessage.builder().sendChatMessageRequest(sendChat).chatRoom(chatRoom).build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);

        return saved;
    }
}
