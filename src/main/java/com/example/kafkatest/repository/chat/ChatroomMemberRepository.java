package com.example.kafkatest.repository.chat;

import com.example.kafkatest.entity.chat.ChatroomMember;
import com.example.kafkatest.entity.chat.ChatroomMemberId;
import com.example.kafkatest.entity.chat.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatroomMemberRepository extends JpaRepository<ChatroomMember, ChatroomMemberId> {
    @Query("select c.member from ChatroomMember c where c.chatroom.id = :chatroomId")
    List<Member> findMemberByChatRoomId(@Param("chatroomId") Long chatroomId);
}
