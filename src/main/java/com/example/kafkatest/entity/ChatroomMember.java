package com.example.kafkatest.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.io.Serializable;

@Entity
public class ChatroomMember implements Serializable {
    @EmbeddedId
    private ChatroomMemberId id;

    @ManyToOne
    @MapsId("chatroomId")
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatroom;

    @ManyToOne
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    private Member member;

    private String role;

    protected ChatroomMember() { }

    @Builder
    protected ChatroomMember(Chatroom chatRoom, Member member, String role) {
        this.chatroom = chatRoom;
        this.member = member;
        this.role = role;
        this.id = ChatroomMemberId.builder().memberId(member.getId()).chatroomId(chatRoom.getId()).build();
    }
}
