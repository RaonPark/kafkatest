package com.example.kafkatest.entity;

import com.example.kafkatest.dto.request.MakeChatRoomRequest;
import com.example.kafkatest.support.BaseUserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "chatroom")
@Getter
@SequenceGenerator(name = "CHATROOM_SEQ", allocationSize = 50, initialValue = 1)
public class Chatroom extends BaseUserEntity implements Serializable {

    @Id
    @Column(name = "chatroom_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHATROOM_SEQ")
    private Long id;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private int numberOfMember;

    @OneToMany(mappedBy = "chatroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<ChatroomMember> chatroomMembers = new HashSet<>();

    protected Chatroom() {
        // empty
    }

    @Builder
    protected Chatroom(MakeChatRoomRequest makeChatRoomRequest) {
        this.roomName = makeChatRoomRequest.getRoomName();
        this.numberOfMember = makeChatRoomRequest.getNumberOfMember();
    }
}
