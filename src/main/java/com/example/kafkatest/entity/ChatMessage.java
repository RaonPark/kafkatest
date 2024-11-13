package com.example.kafkatest.entity;

import com.example.kafkatest.dto.request.SendChatMessageRequest;
import com.example.kafkatest.support.BaseUserEntity;
import com.example.kafkatest.support.ChatMessageType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Entity
@SequenceGenerator(name = "CHATMESSAGE_SEQ", initialValue = 1, allocationSize = 100)
public class ChatMessage extends BaseUserEntity implements Serializable {
    @Getter
    @Id
    @Column(name = "chatmessage_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHATMESSAGE_SEQ")
    private Long id;

    @Getter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatMessageType chatMessageType;

    @Getter
    @Column
    private String message;

    @Getter
    @Column
    private String image;

    @Getter
    @Column
    private String emoticon;

    @Getter
    @Column(nullable = false)
    private int numberOfUnread;

    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatRoom;

    protected ChatMessage() {
        // empty
    }

    @Builder
    protected ChatMessage(SendChatMessageRequest sendChatMessageRequest, Chatroom chatRoom) {
        this.chatMessageType = sendChatMessageRequest.getChatMessageType();
        this.image = sendChatMessageRequest.getImage();
        this.message = sendChatMessageRequest.getMessage();
        this.emoticon = sendChatMessageRequest.getEmoticon();
        this.chatRoom = chatRoom;
        this.numberOfUnread = chatRoom.getNumberOfMember() - 1;
    }
}
